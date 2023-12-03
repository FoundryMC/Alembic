package foundry.alembic;

import com.google.common.collect.*;
import com.mojang.datafixers.util.Pair;
import foundry.alembic.caps.AlembicFlammableProvider;
import foundry.alembic.command.AlembicCommand;
import foundry.alembic.damage.AlembicDamageHandler;
import foundry.alembic.event.AlembicFoodChangeEvent;
import foundry.alembic.items.*;
import foundry.alembic.items.slots.VanillaSlotType;
import foundry.alembic.override.OverrideManager;
import foundry.alembic.resistances.ResistanceManager;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeManager;
import foundry.alembic.types.AlembicGlobalTagPropertyHolder;
import foundry.alembic.types.tag.tags.AlembicHungerTag;
import foundry.alembic.types.tag.tags.AlembicPerLevelTag;
import foundry.alembic.util.AttributeHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.*;

import static foundry.alembic.Alembic.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    public static UUID ALEMBIC_FIRE_RESIST_UUID = UUID.fromString("b3f2b2f0-2b8a-4b9b-9b9b-2b8a4b9b9b9b");
    public static UUID ALEMBIC_FIRE_DAMAGE_UUID = UUID.fromString("e3f2b2f0-2b8a-4b9b-9b9b-2b8a4b9b9b9b");
    public static UUID ALEMBIC_NEGATIVE_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

    public static UUID TEMP_MOD_UUID = UUID.fromString("c3f2b2f0-2b8a-4b9b-9b9b-2b8a4b9b9b9b");
    public static UUID TEMP_MOD_UUID2 = UUID.fromString("d3f2b2f0-2b8a-4b9b-9b9b-2b8a4b9b9b9b");

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        AlembicCommand.register(event.getDispatcher());
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
        event.addListener(new DamageTypeManager(event.getConditionContext()));
        event.addListener(new OverrideManager(event.getConditionContext()));
        event.addListener(new ResistanceManager(event.getConditionContext()));
        event.addListener(new ItemStatManager(event.getConditionContext()));
        event.addListener(new ShieldStatManager(event.getConditionContext()));
    }

    @SubscribeEvent
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        // TODO: note/1
        if (stack.getAllEnchantments().containsKey(Enchantments.FIRE_ASPECT)) {
            int level = stack.getEnchantmentLevel(Enchantments.FIRE_ASPECT);
            Attribute fireDamage = DamageTypeManager.getDamageType("fire_damage").getAttribute();
            if(fireDamage == null) return;
            if(!event.getSlotType().equals(EquipmentSlot.MAINHAND)) return;
            event.addModifier(fireDamage, new AttributeModifier(ALEMBIC_FIRE_DAMAGE_UUID, "Fire Aspect", level, AttributeModifier.Operation.ADDITION));
            event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(ALEMBIC_NEGATIVE_DAMAGE_UUID, "Fire aspect", -(1+level), AttributeModifier.Operation.ADDITION));
        }

        if (!ItemStatManager.hasStats(stack.getItem())) {
            return;
        }
        // TODO: if curios compat is implemented, make sure to do something for it
        ItemStatManager.getStats(stack.getItem(), new VanillaSlotType(event.getSlotType()))
                .forEach(itemStat -> itemStat.computeAttributes(event.getOriginalModifiers(), event::addModifier, event::removeAttribute));
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
                    ResistanceManager.get(le.getType()).getDamage().forEach((alembicDamageType, aFloat) -> {
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

    // TODO: note/2
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void hurt(final LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (isBeingDamaged) {
            Alembic.LOGGER.warn("Alembic is being damaged twice in one tick! This is a bug!");
            return;
        }
        isBeingDamaged = true;
        AlembicDamageHandler.handleDamage(event);
    }

    @SubscribeEvent
    static void onEffect(MobEffectEvent event){
        if (event.getEffectInstance() == null) return;
        AttributeModifier attmod = new AttributeModifier(ALEMBIC_FIRE_RESIST_UUID, "Fire Resistance", 0.1 * (1 + event.getEffectInstance().getAmplifier()), AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (event instanceof MobEffectEvent.Added) {
            if(event.getEffectInstance().getEffect().equals(MobEffects.FIRE_RESISTANCE)){
                Attribute fireRes = DamageTypeManager.getDamageType("fire_damage").getResistanceAttribute();
                if(fireRes == null) return;
                if(event.getEntity().getAttribute(fireRes) == null) return;
                if(event.getEntity().getAttribute(fireRes).getModifier(attmod.getId()) != null) return;
                Objects.requireNonNull(event.getEntity().getAttribute(fireRes)).addPermanentModifier(attmod);
            }
        }
        if (event instanceof MobEffectEvent.Remove) {
            if(event.getEffectInstance().getEffect().equals(MobEffects.FIRE_RESISTANCE)){
                Attribute fireRes = DamageTypeManager.getDamageType("fire_damage").getResistanceAttribute();
                if(fireRes == null) return;
                if(event.getEntity().getAttribute(fireRes) == null) return;
                event.getEntity().getAttribute(fireRes).removeModifier(attmod);
            }
        }
    }

    private static AttributeModifier FIRE_WATER_ATT_MOD = new AttributeModifier(ALEMBIC_FIRE_RESIST_UUID, "Fire Resistance", 6, AttributeModifier.Operation.ADDITION);

    @SubscribeEvent
    public static void livingTick(LivingEvent.LivingTickEvent event){
        if(!event.getEntity().level().isClientSide){
            if(event.getEntity().isInWaterOrRain()){
                // if the entity is in water or rain, increase fire resistance by +6
                Attribute fireRes = DamageTypeManager.getDamageType("fire_damage").getResistanceAttribute();
                if(fireRes == null) return;
                if(event.getEntity().getAttribute(fireRes) == null) return;
                if(event.getEntity().getAttribute(fireRes).getModifier(FIRE_WATER_ATT_MOD.getId()) != null) return;
                event.getEntity().getAttribute(fireRes).addPermanentModifier(FIRE_WATER_ATT_MOD);
            } else {
                // if the entity is not in water or rain, remove the fire resistance modifier
                Attribute fireRes = DamageTypeManager.getDamageType("fire_damage").getResistanceAttribute();
                if(fireRes == null) return;
                if(event.getEntity().getAttribute(fireRes) == null) return;
                if(event.getEntity().getAttribute(fireRes).getModifier(FIRE_WATER_ATT_MOD.getId()) == null) return;
                event.getEntity().getAttribute(fireRes).removeModifier(FIRE_WATER_ATT_MOD);
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

                AttributeModifier modifier = new AttributeModifier(tag.getUUID(), modifierId, scalar, tag.getOperation());

                if (instance.getModifier(tag.getUUID()) != null) {
                    instance.removeModifier(tag.getUUID());
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