package foundry.alembic;

import foundry.alembic.attribute.AlembicAttribute;
import foundry.alembic.attribute.UUIDFactory;
import foundry.alembic.attribute.UUIDManager;
import foundry.alembic.caps.AlembicFlammableProvider;
import foundry.alembic.command.AlembicCommand;
import foundry.alembic.damage.AlembicDamageHandler;
import foundry.alembic.event.AlembicFoodChangeEvent;
import foundry.alembic.networking.AlembicPacketHandler;
import foundry.alembic.networking.ClientboundSyncItemStatsPacket;
import foundry.alembic.override.OverrideManager;
import foundry.alembic.stats.entity.StatsManager;
import foundry.alembic.stats.item.ItemStatManager;
import foundry.alembic.stats.item.slots.EquipmentSlotType;
import foundry.alembic.stats.item.slots.VanillaSlotType;
import foundry.alembic.stats.shield.ShieldBlockStat;
import foundry.alembic.stats.shield.ShieldStatManager;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.AlembicGlobalTagPropertyHolder;
import foundry.alembic.types.DamageTypeManager;
import foundry.alembic.types.tag.tags.AlembicHungerTag;
import foundry.alembic.types.tag.tags.AlembicPerLevelTag;
import foundry.alembic.util.AttributeHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static foundry.alembic.Alembic.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    public static UUID ALEMBIC_FIRE_RESIST_UUID = UUID.fromString("b3f2b2f0-2b8a-4b9b-9b9b-2b8a4b9b9b9b");
    public static UUID ALEMBIC_FIRE_DAMAGE_UUID = UUID.fromString("e3f2b2f0-2b8a-4b9b-9b9b-2b8a4b9b9b9b");
    public static UUID ALEMBIC_NEGATIVE_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

    public static UUID TEMP_MOD_UUID = UUID.fromString("c3f2b2f0-2b8a-4b9b-9b9b-2b8a4b9b9b9b");
    public static UUID TEMP_MOD_UUID2 = UUID.fromString("d3f2b2f0-2b8a-4b9b-9b9b-2b8a4b9b9b9b");

    // This is only valid during data reload
    private static ICondition.IContext conditionContext;
    public static ICondition.IContext getCurrentContext() {
        return conditionContext;
    }

    @SubscribeEvent
    static void onServerClose(final ServerStoppedEvent event) {
        AlembicAttribute.clearCache();
    }

    @SubscribeEvent
    static void registerCommands(RegisterCommandsEvent event) {
        AlembicCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    static void syncItemStats(final OnDatapackSyncEvent event) {
        PacketDistributor.PacketTarget packetTarget;
        if (event.getPlayer() != null) {
            packetTarget = PacketDistributor.PLAYER.with(event::getPlayer);
        } else {
            packetTarget = PacketDistributor.ALL.noArg();
        }
        AlembicPacketHandler.INSTANCE.send(packetTarget, new ClientboundSyncItemStatsPacket(ItemStatManager.getStats()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onLevelUp(PlayerXpEvent.LevelChange event) {
        Player player = event.getEntity();
        if(player.level().isClientSide) return;
        for (AlembicPerLevelTag tag : AlembicGlobalTagPropertyHolder.getLevelupBonuses(player.experienceLevel + event.getLevels())) {
            // Get the upgrade region
            RangedAttribute attribute = tag.getAffectedAttribute();
            ResourceLocation modifierId = tag.getModifierId();

            int playerRegion = getTagDataElement(player, modifierId.toString());

            if (player.getAttributes().hasAttribute(attribute)) {
                if (playerRegion < tag.getCap()) {
                    AttributeHelper.addOrModifyModifier(player, attribute, modifierId, tag.getBonus());

                    // Write the number of level-ups for the attribute
                    setTagDataElement(player, modifierId.toString(), playerRegion+=tag.getBonus());
                }

                if (Alembic.isDebugEnabled()) {
                    player.displayClientMessage(Component.literal(modifierId + ": " + playerRegion), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> event) {
        event.addCapability(Alembic.location("fire_tag"), new AlembicFlammableProvider());
    }

    @SubscribeEvent
    static void onJsonListener(AddReloadListenerEvent event) {
        conditionContext = event.getConditionContext();
        event.addListener(new DamageTypeManager(conditionContext, event.getRegistryAccess()));
        event.addListener(new OverrideManager(conditionContext, event.getRegistryAccess()));
        event.addListener(new StatsManager(conditionContext));
        event.addListener(new ItemStatManager(conditionContext));
        event.addListener(new ShieldStatManager(conditionContext));
    }

    @SubscribeEvent
    static void resetConditionContext(TagsUpdatedEvent event) {
        conditionContext = null;
    }

    @SubscribeEvent
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        // TODO: note/1
        if (stack.getAllEnchantments().containsKey(Enchantments.FIRE_ASPECT)) {
            int level = stack.getEnchantmentLevel(Enchantments.FIRE_ASPECT);
            AlembicDamageType fireDamage = DamageTypeManager.getDamageType("fire_damage");
            if(fireDamage == null) return;
            Attribute fireDamageAttr = fireDamage.getAttribute();
            if(!event.getSlotType().equals(EquipmentSlot.MAINHAND)) return;
            event.addModifier(fireDamageAttr, new AttributeModifier(ALEMBIC_FIRE_DAMAGE_UUID, "Fire Aspect", level, AttributeModifier.Operation.ADDITION));
            event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(ALEMBIC_NEGATIVE_DAMAGE_UUID, "Fire aspect", -(1+level), AttributeModifier.Operation.ADDITION));
        }

        if (!ItemStatManager.hasStats(stack.getItem())) {
            return;
        }
        // TODO: if curios compat is implemented, make sure to do something for it
        EquipmentSlotType slotType = new VanillaSlotType(event.getSlotType());
        ItemStatManager.getStats(stack.getItem(), slotType)
                .forEach(itemStat -> itemStat.computeAttributes(event.getModifiers(), event::addModifier, event::removeAttribute, slotType));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void cancelShieldBlock(ShieldBlockEvent event) {
        if (!OverrideManager.containsKey(event.getDamageSource())) {
            return;
        }
        ItemStack blockingItem = ItemStack.EMPTY;
        if(event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).canPerformAction(ToolActions.SHIELD_BLOCK)){
            blockingItem = event.getEntity().getItemInHand(InteractionHand.MAIN_HAND);
        } else if(event.getEntity().getItemInHand(InteractionHand.OFF_HAND).canPerformAction(ToolActions.SHIELD_BLOCK)){
            blockingItem = event.getEntity().getItemInHand(InteractionHand.OFF_HAND);
        }
        if(!blockingItem.isEmpty()){
            Collection<ShieldBlockStat> stats = ShieldStatManager.getStats(blockingItem.getItem());
            LivingEntity entity = event.getEntity();
            MutableFloat finalDamage = new MutableFloat();

            // Need to partition damage to each damage type, then block whatever amount from each, and sum up to put back into the event.
            OverrideManager.getOverridesForSource(event.getDamageSource()).getDamagePercents()
                    .forEach((alembicDamageType, aFloat) -> {
                        float damagePart = event.getBlockedDamage() * aFloat;

                        RangedAttribute resistanceAttribute = alembicDamageType.getResistanceAttribute();
                        AttributeInstance instance = entity.getAttribute(resistanceAttribute);
                        if (instance == null) {
                            return;
                        }

                        damagePart -= (float) instance.getValue();
                        for(ShieldBlockStat stat : stats){
                            for(ShieldBlockStat.TypeModifier mod : stat.typeModifiers()){
                                if(mod.type().equals(alembicDamageType.getId())){
                                    damagePart *= mod.modifier();
                                }
                            }
                        }
                        finalDamage.add(Math.max(damagePart, 0));

                    });
            if(event.getDamageSource().getDirectEntity() != null){
                if(event.getDamageSource().getDirectEntity() instanceof LivingEntity le){
                    StatsManager.get(le.getType()).getDamage().forEach((alembicDamageType, aFloat) -> {
                        float damagePart = event.getBlockedDamage() * aFloat;

                        RangedAttribute resistanceAttribute = alembicDamageType.getResistanceAttribute();
                        AttributeInstance instance = entity.getAttribute(resistanceAttribute);
                        if (instance == null) {
                            return;
                        }

                        damagePart -= (float) instance.getValue();
                        for(ShieldBlockStat stat : stats){
                            for(ShieldBlockStat.TypeModifier mod : stat.typeModifiers()){
                                if(mod.type().equals(alembicDamageType.getId())){
                                    damagePart *= mod.modifier();
                                }
                            }
                        }
                        finalDamage.add(Math.max(damagePart, 0));
                    });
                }
            }
            event.setBlockedDamage(event.getBlockedDamage() - finalDamage.getValue());
        }
    }

    public static boolean isBeingDamaged = false;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void hurt(final LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (isBeingDamaged) {
            Alembic.printInDebug(() -> "Warning, damaging twice. If this is not a highly modded environment, report this.");
            if(AlembicConfig.ignoreDoubleDamage.get()) return;
        }
        isBeingDamaged = true;
        AlembicDamageHandler.handleDamage(event);
    }

    @SubscribeEvent
    static void onEffect(MobEffectEvent event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) return;
        AttributeModifier attmod = new AttributeModifier(ALEMBIC_FIRE_RESIST_UUID, "Fire Resistance", 0.1 * (1 + event.getEffectInstance().getAmplifier()), AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (event instanceof MobEffectEvent.Added) {
            if(instance.getEffect() == MobEffects.FIRE_RESISTANCE) {
                AlembicDamageType damageType = DamageTypeManager.getDamageType("fire_damage");
                if (damageType == null) return;
                Attribute fireRes = damageType.getResistanceAttribute();
                if(fireRes == null) return;
                if(event.getEntity().getAttribute(fireRes) == null) return;
                if(event.getEntity().getAttribute(fireRes).getModifier(attmod.getId()) != null) return;
                Objects.requireNonNull(event.getEntity().getAttribute(fireRes)).addPermanentModifier(attmod);
            }
        }
        if (event instanceof MobEffectEvent.Remove) {
            if(instance.getEffect() == MobEffects.FIRE_RESISTANCE) {
                AlembicDamageType damageType = DamageTypeManager.getDamageType("fire_damage");
                if (damageType == null) return;
                Attribute fireRes = damageType.getResistanceAttribute();
                if(fireRes == null) return;
                if(event.getEntity().getAttribute(fireRes) == null) return;
                event.getEntity().getAttribute(fireRes).removeModifier(attmod);
            }
        }
    }

    private static final AttributeModifier FIRE_WATER_ATT_MOD = new AttributeModifier(ALEMBIC_FIRE_RESIST_UUID, "Fire Resistance", 6, AttributeModifier.Operation.ADDITION);

    @SubscribeEvent
    public static void livingTick(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide) {
            if (event.getEntity().isInWaterOrRain()) {
                // if the entity is in water or rain, increase fire resistance by +6
                AlembicDamageType fireDamage = DamageTypeManager.getDamageType("fire_damage");
                if (fireDamage == null) return;
                Attribute fireRes = fireDamage.getResistanceAttribute();
                AttributeInstance attributeInstance = event.getEntity().getAttribute(fireRes);
                if (attributeInstance == null) return;
                if (attributeInstance.getModifier(FIRE_WATER_ATT_MOD.getId()) != null) return;
                attributeInstance.addPermanentModifier(FIRE_WATER_ATT_MOD);
            } else {
                // if the entity is not in water or rain, remove the fire resistance modifier
                AlembicDamageType fireDamage = DamageTypeManager.getDamageType("fire_damage");
                if (fireDamage == null) return;
                Attribute fireRes = fireDamage.getResistanceAttribute();
                AttributeInstance attributeInstance = event.getEntity().getAttribute(fireRes);
                if (attributeInstance == null) return;
                if (attributeInstance.getModifier(FIRE_WATER_ATT_MOD.getId()) == null) return;
                attributeInstance.removeModifier(FIRE_WATER_ATT_MOD);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST) // We don't want to modify, but we do want to get the final values
    static void hungerChanged(AlembicFoodChangeEvent event) {
        if (event.getPlayer().level().isClientSide) return;

        Player player = event.getPlayer();
        int hungerValue = event.getFoodLevel();

        for (Map.Entry<AlembicDamageType, AlembicHungerTag> entry : AlembicGlobalTagPropertyHolder.getHungerBonuses().entrySet()) {
            AlembicDamageType type = entry.getKey();
            AlembicHungerTag tag = entry.getValue();
            float affectedFraction = (float)hungerValue / (float)tag.getHungerTrigger();
            String modifierId = "%s.%s_hunger_mod".formatted(type.createTranslationString(), tag.getTypeModifier());

            int playerRegion = getTagDataElement(player, modifierId);
            if (playerRegion == (int)affectedFraction && affectedFraction % 1 != 0) {
                return;
            }
            setTagDataElement(player, modifierId, (int)affectedFraction);

            RangedAttribute attribute = tag.getTypeModifier().getAffectedAttribute(type);
            AttributeInstance instance = player.getAttribute(attribute);

            if (instance != null) { // maybe log warning?
                float scalar = tag.getScaleAmount() * ((20 / tag.getHungerTrigger()) - (int)affectedFraction);

                UUID uuid = UUIDFactory.getOrCreate(player.level(), type.getId().withSuffix(".%s_hunger_mod".formatted(tag.getTypeModifier().getSerializedName())));

                AttributeModifier modifier = new AttributeModifier(uuid, modifierId, scalar, tag.getOperation());

                if (instance.getModifier(uuid) != null) {
                    instance.removeModifier(uuid);
                    instance.addTransientModifier(modifier);
                } else {
                    instance.addTransientModifier(modifier);
                }

                if (Alembic.isDebugEnabled()) {
                    player.displayClientMessage(Component.literal(modifierId + " Resistance: " + instance.getValue()), true);
                }
            }
        }
    }

    private static int getTagDataElement(Player player, String id) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains("AlembicTagData")) {
            return 0;
        }
        CompoundTag tagData = persistentData.getCompound("AlembicTagData");
        if (!tagData.contains(id)) {
            return 0;
        }
        return tagData.getInt(id);
    }

    private static void setTagDataElement(Player player, String id, int region) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains("AlembicTagData")) {
            persistentData.put("AlembicTagData", new CompoundTag());
        }
        CompoundTag tagData = persistentData.getCompound("AlembicTagData");
        tagData.putInt(id, region);
    }
}