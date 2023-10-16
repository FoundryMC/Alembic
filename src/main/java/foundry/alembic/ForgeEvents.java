package foundry.alembic;

import com.google.common.collect.*;
import foundry.alembic.caps.AlembicFlammableProvider;
import foundry.alembic.damagesource.DamageSourceIdentifier;
import foundry.alembic.event.AlembicDamageDataModificationEvent;
import foundry.alembic.event.AlembicDamageEvent;
import foundry.alembic.event.AlembicFoodChangeEvent;
import foundry.alembic.items.*;
import foundry.alembic.items.modifiers.ItemModifier;
import foundry.alembic.items.modifiers.AppendItemModifier;
import foundry.alembic.items.slots.VanillaSlotType;
import foundry.alembic.networking.AlembicPacketHandler;
import foundry.alembic.networking.ClientboundAlembicDamagePacket;
import foundry.alembic.override.AlembicOverride;
import foundry.alembic.override.OverrideManager;
import foundry.alembic.resistances.AlembicResistance;
import foundry.alembic.resistances.ResistanceManager;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeManager;
import foundry.alembic.types.AlembicGlobalTagPropertyHolder;
import foundry.alembic.types.tag.tags.AlembicHungerTag;
import foundry.alembic.types.tag.tags.AlembicPerLevelTag;
import foundry.alembic.util.AttributeHelper;
import foundry.alembic.util.ComposedData;
import foundry.alembic.util.ComposedDataTypes;
import foundry.alembic.util.Utils;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onLevelUp(PlayerXpEvent.LevelChange event) {
        Player player = event.getEntity();
        if(player.level.isClientSide) return;
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

    @SubscribeEvent
    static void applyUsedAttributes(LivingEntityUseItemEvent.Start event) {
        ItemStack stack = event.getItem();
        if (!ItemStatManager.hasStats(stack.getItem())) {
            return;
        }

        LivingEntity entity = event.getEntity();
        EquipmentSlot slot = Utils.equipmentFromHand(entity.getUsedItemHand());
        for (ItemStat stat : ItemStatManager.getStats(stack.getItem(), new VanillaSlotType(slot))) {
            for (ItemModifier modifier : stat.getItemModifiers()) {
                if (modifier instanceof AppendItemModifier appendItemModifier && appendItemModifier.getApplication() == ModifierApplication.USED) {
                    AttributeMap map = entity.getAttributes();
                    if (map.hasAttribute(appendItemModifier.getAttribute())) {
                        AttributeModifier attributeModifier = new AttributeModifier(
                                appendItemModifier.getUUID(),
                                appendItemModifier.getAttribute().descriptionId,
                                appendItemModifier.getValue(),
                                appendItemModifier.getOperation()
                        );
                        map.getInstance(appendItemModifier.getAttribute()).addTransientModifier(attributeModifier);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    static void removeUsedAttributes(LivingEntityUseItemEvent.Stop event) {
        ItemStack stack = event.getItem();
        if (!ItemStatManager.hasStats(stack.getItem())) {
            return;
        }

        LivingEntity entity = event.getEntity();
        EquipmentSlot slot = Utils.equipmentFromHand(entity.getUsedItemHand());
        for (ItemStat stat : ItemStatManager.getStats(stack.getItem(), new VanillaSlotType(slot))) {
            for (ItemModifier modifier : stat.getItemModifiers()) {
                if (modifier instanceof AppendItemModifier appendItemModifier && appendItemModifier.getApplication() == ModifierApplication.USED) {
                    AttributeMap map = entity.getAttributes();
                    if (map.hasAttribute(appendItemModifier.getAttribute())) {
                        map.getInstance(appendItemModifier.getAttribute()).removeModifier(appendItemModifier.getUUID());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingSpawn(final LivingSpawnEvent event) {
//        if(!event.getLevel().isClientSide()){
//            AlembicResistance stats = AlembicResistanceHolder.get(event.getEntity().getType());
//            if(stats != null){
//                stats.getResistances().forEach((damageType, value) -> {
//                    AttributeInstance resistanceInstance = event.getEntity().getAttribute(damageType.getResistanceAttribute());
//                    if(resistanceInstance != null){
//                        resistanceInstance.setBaseValue(value);
//                    }
//                });
//            }
//        }
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

    private static boolean isBeingDamaged = false;

    // TODO: note/2
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void hurt(final LivingHurtEvent event) {
        LivingEntity target = event.getEntity();

        if (target.level.isClientSide) return;
        if (isBeingDamaged) {
            return;
        }

        DamageSource originalDamageSource = event.getSource();
        isBeingDamaged = true;

        if (Alembic.isDebugEnabled()) {
            Alembic.LOGGER.info("Handling hurt event for {} with source {} and amount {}", target.getName().getString(), originalDamageSource.getMsgId(), event.getAmount());
            Alembic.LOGGER.info("Source is {} and is projectile? {}", originalDamageSource.getDirectEntity(), originalDamageSource.getDirectEntity() instanceof Projectile);
        }

        if (originalDamageSource instanceof IndirectEntityDamageSource || originalDamageSource.getDirectEntity() == null || (originalDamageSource.getDirectEntity() instanceof AbstractArrow && !AlembicConfig.ownerAttributeProjectiles.get())
                || originalDamageSource.getDirectEntity() instanceof AbstractHurtingProjectile || (originalDamageSource.getDirectEntity() instanceof Projectile)) {
            isBeingDamaged = false;
            float totalDamage = event.getAmount();
            AlembicOverride override = OverrideManager.getOverridesForSource(originalDamageSource);
            Alembic.printInDebug(() -> "Found override for " + originalDamageSource.getMsgId() + " with damage " + totalDamage + ". " + override);
            if (override != null) {
                handleTypedDamage(target, null, totalDamage, override, originalDamageSource);
                //target.hurt(e.getSource(), totalDamage);
                isBeingDamaged = false;
                event.setCanceled(true);
            } else {
                isBeingDamaged = false;
                return;
            }
        } else if (originalDamageSource.getDirectEntity() instanceof LivingEntity || (originalDamageSource.getDirectEntity() instanceof AbstractArrow && AlembicConfig.ownerAttributeProjectiles.get())) {
            LivingEntity attacker;
            if(originalDamageSource.getDirectEntity() instanceof AbstractArrow) {
                attacker = (LivingEntity) ((AbstractArrow) originalDamageSource.getDirectEntity()).getOwner();
            } else {
                attacker = (LivingEntity) originalDamageSource.getDirectEntity();
            }
            if(attacker == null) {
                isBeingDamaged = false;
                return;
            }
            Multimap<Attribute, AttributeModifier> map = ArrayListMultimap.create();
            if (handleEnchantments(attacker, target, map)) return;
            float totalDamage = event.getAmount();
            AlembicResistance stats = ResistanceManager.get(target.getType());
            boolean entityOverride = stats != null;
            float damageOffset = 0;
            if (entityOverride) {
                damageOffset = handleTypedDamage(target, attacker, totalDamage, stats, originalDamageSource);
            }
            totalDamage -= damageOffset;
            for (AlembicDamageType damageType : DamageTypeManager.getDamageTypes()) {
                if(attacker.getAttribute(damageType.getAttribute()) == null) continue;
                if (attacker.getAttribute(damageType.getAttribute()).getValue() > 0 && !entityOverride) {
                    float damage = (float) attacker.getAttribute(damageType.getAttribute()).getValue();
//                    if(target.isBlocking() && !damageType.getId().getPath().contains("true_damage")) {
//                        ItemStack blockingItem = ItemStack.EMPTY;
//                        if(target.getItemInHand(InteractionHand.MAIN_HAND).canPerformAction(ToolActions.SHIELD_BLOCK)){
//                            blockingItem = target.getItemInHand(InteractionHand.MAIN_HAND);
//                        } else if(target.getItemInHand(InteractionHand.OFF_HAND).canPerformAction(ToolActions.SHIELD_BLOCK)){
//                            blockingItem = target.getItemInHand(InteractionHand.OFF_HAND);
//                        }
//                        if(!blockingItem.isEmpty()){
//                            Collection<ShieldBlockStat> stats1 = ShieldStatManager.getStats(blockingItem.getItem());
//                            for(ShieldBlockStat stat : stats1){
//                                for(ShieldBlockStat.TypeModifier mod : stat.typeModifiers()){
//                                    if(mod.type().equals(damageType.getId())){
//                                        damage *= mod.modifier();
//                                    }
//                                }
//                            }
//                        }
//                    }
                    float attrValue = target.getAttributes().hasAttribute(damageType.getResistanceAttribute()) ? (float) target.getAttribute(damageType.getResistanceAttribute()).getValue() : 0;
                    AlembicDamageEvent.Pre preDamage = new AlembicDamageEvent.Pre(target, attacker, damageType, damage, attrValue);
                    MinecraftForge.EVENT_BUS.post(preDamage);
                    damage = preDamage.getDamage();
                    attrValue = preDamage.getResistance();
                    if (damage <= 0 || preDamage.isCanceled()) return;
                    damage = CombatRules.getDamageAfterAbsorb(damage, attrValue, (float) target.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue());

                    handleDamageInstance(target, damageType, damage, originalDamageSource);
                    AlembicDamageEvent.Post postDamage = new AlembicDamageEvent.Post(target, attacker, damageType, damage, attrValue);
                    MinecraftForge.EVENT_BUS.post(postDamage);
                }
            }
            int time = target.invulnerableTime;
            target.invulnerableTime = 0;
            if (totalDamage + 0.001f >= 0.001) {
                target.hurt(src(attacker), totalDamage + 0.001f);
            }
            target.invulnerableTime = time;
            attacker.getAttributes().attributes.forEach((attribute, attributeInstance) -> {
                if (attributeInstance.getModifier(TEMP_MOD_UUID) != null) {
                    attributeInstance.removeModifier(TEMP_MOD_UUID);
                }
                if(attributeInstance.getModifier(TEMP_MOD_UUID2) != null) {
                    attributeInstance.removeModifier(TEMP_MOD_UUID2);
                }
            });
        }
        isBeingDamaged = false;
        event.setCanceled(true);
    }

    private static boolean handleEnchantments(LivingEntity attacker, LivingEntity target, Multimap<Attribute, AttributeModifier> map) {
        for(Map.Entry<Enchantment, Integer> entry : attacker.getItemInHand(InteractionHand.MAIN_HAND).getAllEnchantments().entrySet()){
            if(entry.getKey().equals(Enchantments.BANE_OF_ARTHROPODS)){
                if(target.getMobType() == MobType.ARTHROPOD){
                    Attribute alchDamage = DamageTypeManager.getDamageType("arcane_damage").getAttribute();
                    if(alchDamage == null) return true;
                    AttributeModifier attributeModifier = new AttributeModifier(TEMP_MOD_UUID, "Bane of Arthropods", entry.getValue(), AttributeModifier.Operation.ADDITION);
                    attacker.getAttribute(alchDamage).addTransientModifier(attributeModifier);
                    map.put(alchDamage, attributeModifier);
                }
            } else if (entry.getKey().equals(Enchantments.SMITE)){
                if(target.getMobType() == MobType.UNDEAD){
                    Attribute alchDamage = DamageTypeManager.getDamageType("arcane_damage").getAttribute();
                    if(alchDamage == null) return true;
                    AttributeModifier attributeModifier = new AttributeModifier(TEMP_MOD_UUID2, "Smite", entry.getValue(), AttributeModifier.Operation.ADDITION);
                    attacker.getAttribute(alchDamage).addTransientModifier(attributeModifier);
                    map.put(alchDamage, attributeModifier);
                }
            } else if (entry.getKey().equals(Enchantments.IMPALING)){
                if(target.isInWaterOrRain()){
                    Attribute alchDamage = DamageTypeManager.getDamageType("arcane_damage").getAttribute();
                    if(alchDamage == null) return true;
                    AttributeModifier attributeModifier = new AttributeModifier(TEMP_MOD_UUID, "Impaling", entry.getValue(), AttributeModifier.Operation.ADDITION);
                    attacker.getAttribute(alchDamage).addTransientModifier(attributeModifier);
                    map.put(alchDamage, attributeModifier);
                }
            }
        } return false;
    }

    private static void handleDamageInstance(LivingEntity target, AlembicDamageType damageType, float damage, DamageSource originalSource) {
        damage = Math.round(Math.max(damage, 0)*10)/10f;
        if (damage > 0) {
            float finalDamage = damage;
            damageType.getTags().forEach(tag -> {
                ComposedData data = ComposedData.createEmpty()
                        .add(ComposedDataTypes.SERVER_LEVEL, (ServerLevel) target.level)
                        .add(ComposedDataTypes.TARGET_ENTITY, target)
                        .add(ComposedDataTypes.FINAL_DAMAGE, finalDamage)
                        .add(ComposedDataTypes.ORIGINAL_SOURCE, originalSource)
                        .add(ComposedDataTypes.DAMAGE_TYPE, damageType);
                AlembicDamageDataModificationEvent event = new AlembicDamageDataModificationEvent(data);
                MinecraftForge.EVENT_BUS.post(event);
                data = event.getData();
                if (tag.testConditions(data)) {
                    tag.onDamage(data);
                }
            });
            int invtime = target.invulnerableTime;
            target.invulnerableTime = 0;
            float f2 = Math.max(damage - target.getAbsorptionAmount(), 0.0F);
            target.setAbsorptionAmount(target.getAbsorptionAmount() - (damage - f2));
            float f = damage - f2;
            if (f > 0.0F && f < 3.4028235E37F && target instanceof Player) {
                ((Player) target).awardStat(Stats.CUSTOM.get(Stats.DAMAGE_DEALT_ABSORBED), Math.round(f * 10.0F));
            }
            float health = target.getHealth();
            target.getCombatTracker().recordDamage(DamageSource.GENERIC, health, damage);
            target.setHealth(health - damage);
            target.setAbsorptionAmount(target.getAbsorptionAmount() - f2);
            sendDamagePacket(target, damageType, damage);
            target.gameEvent(GameEvent.ENTITY_DAMAGE);
            target.invulnerableTime = invtime;
            if (Alembic.isDebugEnabled()) {
                Alembic.LOGGER.info("Dealt damage of type " + damageType.getId() + " to " + target.getName().getString() + " for " + damage + " damage.");
            }
        }
    }

    private static void sendDamagePacket(LivingEntity target, AlembicDamageType damageType, float damage) {
        AlembicPacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(() ->
                        new PacketDistributor.TargetPoint(target.getX(), target.getY(), target.getZ(), 128, target.level.dimension())),
                new ClientboundAlembicDamagePacket(target.getId(), damageType.getId().toString(), damage, damageType.getColor()));
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

    private static void handleTypedDamage(LivingEntity target, LivingEntity attacker, float totalDamage, AlembicOverride override, DamageSource originalSource) {
        for (Object2FloatMap.Entry<AlembicDamageType> entry : override.getDamagePercents().object2FloatEntrySet()) {
            AlembicDamageType damageType = entry.getKey();
            float percentage = entry.getFloatValue();
            float damage = totalDamage * percentage;
            if (AlembicConfig.enableDebugPrints.get()) {
                Alembic.LOGGER.info("Damage: {} Type: {} Total: {} Percentage: {} Final Damage: {}", damage, damageType.getId(), totalDamage, percentage, totalDamage - damage);
            }
            totalDamage -= damage;
            if (AlembicConfig.ownerAttributeProjectiles.get() && attacker != null) {
                if (attacker.getAttribute(damageType.getAttribute()) != null) {
                    double attrValue = attacker.getAttribute(damageType.getAttribute()).getValue();
                    damage += attrValue;
                }
            }
            if (damage <= 0) {
                Alembic.LOGGER.warn("Damage overrides are too high! Damage is being reduced to 0 for {}!", damageType.getId());
                continue;
            }
            damageCalc(target, attacker, damageType, damage, originalSource);
        }
    }

    private static AttributeModifier FIRE_WATER_ATT_MOD = new AttributeModifier(ALEMBIC_FIRE_RESIST_UUID, "Fire Resistance", 6, AttributeModifier.Operation.ADDITION);

    @SubscribeEvent
    public static void livingTick(LivingEvent.LivingTickEvent event){
        if(!event.getEntity().level.isClientSide){
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

    private static float handleTypedDamage(LivingEntity target, LivingEntity attacker, float totalDamage, AlembicResistance stats, DamageSource originalSource) {
        MutableFloat total = new MutableFloat();
        stats.getResistances().forEach((alembicDamageType, multiplier) -> {
            if (stats.getIgnoredSources().stream().map(DamageSourceIdentifier::getSerializedName).toList().contains(originalSource.msgId)) return;
            AttributeInstance i = attacker.getAttribute(alembicDamageType.getAttribute());
            if (i == null) return;
            // multiplier is 0-2, 1 is normal damage, 2 is no damage, 0 is double damage. scale the value of i by this
            if(multiplier < 1){
                multiplier = 1 + (1 - multiplier);
            } else {
                multiplier = 1 - (multiplier - 1);
            }
            float damage = (float) (i.getValue() * multiplier);
//            if(target.isBlocking() && !alembicDamageType.getId().getPath().contains("true_damage")) {
//                ItemStack blockingItem = ItemStack.EMPTY;
//                if(target.getItemInHand(InteractionHand.MAIN_HAND).canPerformAction(ToolActions.SHIELD_BLOCK)){
//                    blockingItem = target.getItemInHand(InteractionHand.MAIN_HAND);
//                } else if(target.getItemInHand(InteractionHand.OFF_HAND).canPerformAction(ToolActions.SHIELD_BLOCK)){
//                    blockingItem = target.getItemInHand(InteractionHand.OFF_HAND);
//                }
//                if(!blockingItem.isEmpty()){
//                    Collection<ShieldBlockStat> stats1 = ShieldStatManager.getStats(blockingItem.getItem());
//                    for(ShieldBlockStat stat : stats1){
//                        for(ShieldBlockStat.TypeModifier mod : stat.typeModifiers()){
//                            if(mod.type().equals(alembicDamageType.getId())){
//                                damage *= mod.modifier();
//                            }
//                        }
//                    }
//                }
//            }
            if (damage < 0) {
                Alembic.LOGGER.warn("Damage overrides are too high! Damage is being reduced to 0 for {}!", alembicDamageType.getId().toString());
                return;
            }
            total.add(damage);
            damageCalc(target, attacker, alembicDamageType, damage, originalSource);
        });
        return total.getValue();
    }

    private static void damageCalc(LivingEntity target, LivingEntity attacker, AlembicDamageType alembicDamageType, float damage, DamageSource originalSource) {
        float attrValue = target.getAttributes().hasAttribute(alembicDamageType.getResistanceAttribute()) ? (float) target.getAttribute(alembicDamageType.getResistanceAttribute()).getValue() : 0;
        AlembicDamageEvent.Pre preDamage = new AlembicDamageEvent.Pre(target, attacker, alembicDamageType, damage, attrValue);
        MinecraftForge.EVENT_BUS.post(preDamage);
        damage = preDamage.getDamage();
        if(attacker instanceof Player pe){
            damage *= pe.getAttackStrengthScale(0.5F);
        }
        attrValue = preDamage.getResistance();
        if (damage <= 0 || preDamage.isCanceled()) return;
        target.hurtArmor(originalSource, damage);
        damage = CombatRules.getDamageAfterAbsorb(damage, attrValue, (float) target.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue());
        damage = AlembicDamageHelper.getDamageAfterAttributeAbsorb(target, alembicDamageType, damage);
        boolean enchantReduce = alembicDamageType.hasEnchantReduction();
        if(enchantReduce) {
            int k = EnchantmentHelper.getDamageProtection(target.getArmorSlots(), DamageSource.mobAttack(attacker));
            if (k > 0) {
                damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float)k);
            }
        }
        float absorptionValue = target.getAttributes().hasAttribute(alembicDamageType.getAbsorptionAttribute()) ? (float) target.getAttribute(alembicDamageType.getAbsorptionAttribute()).getValue() : 0;
        if (absorptionValue > 0) {
            float absorption = Math.min(absorptionValue, damage);
            absorptionValue -= absorption;
            absorptionValue = Math.max(0, absorptionValue);
            damage -= absorption;
            target.getAttribute(alembicDamageType.getAbsorptionAttribute()).setBaseValue(absorptionValue);
        }
        handleDamageInstance(target, alembicDamageType, damage, originalSource);
        AlembicDamageEvent.Post postDamage = new AlembicDamageEvent.Post(target, attacker, alembicDamageType, damage, attrValue);
        MinecraftForge.EVENT_BUS.post(postDamage);
    }

//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    static void attack(final LivingAttackEvent e) {
//    }

    private static DamageSource src(LivingEntity entity) {
        return entity instanceof Player p ? DamageSource.playerAttack(p) : DamageSource.mobAttack(entity);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST) // We don't want to modify, but we do want to get the final values
    static void hungerChanged(AlembicFoodChangeEvent event) {
        if (event.getPlayer().level.isClientSide) return;

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

    // Use to clear DamageTypeRegistry, ItemStatHolder, and AlembicResistanceHolder
//    @SubscribeEvent
//    static void clearDatapackElements(final LoggingOut event) {
//
//    }
}