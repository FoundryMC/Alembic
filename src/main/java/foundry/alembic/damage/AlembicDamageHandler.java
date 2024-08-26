package foundry.alembic.damage;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import foundry.alembic.Alembic;
import foundry.alembic.AlembicConfig;
import foundry.alembic.event.AlembicDamageDataModificationEvent;
import foundry.alembic.event.AlembicDamageEvent;
import foundry.alembic.networking.AlembicPacketHandler;
import foundry.alembic.networking.ClientboundAlembicDamagePacket;
import foundry.alembic.override.AlembicOverride;
import foundry.alembic.override.OverrideManager;
import foundry.alembic.stats.entity.AlembicEntityStats;
import foundry.alembic.stats.entity.EntityStatsManager;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeManager;
import foundry.alembic.types.tag.AlembicTagRegistry;
import foundry.alembic.util.ComposedData;
import foundry.alembic.util.ComposedDataTypes;
import it.unimi.dsi.fastutil.doubles.DoubleFloatPair;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static foundry.alembic.ForgeEvents.*;

public class AlembicDamageHandler {
    public static void handleDamage(LivingHurtEvent event) {
        if (event.getAmount() <= 0) {
            return;
        }
        LivingEntity target = event.getEntity();
        DamageSource originalSource = event.getSource();
        Alembic.printInDebug(() -> {
            return "Handling damage for " + target + " with source " + originalSource.getMsgId() + ". Is indirect: " + isIndirect(originalSource) + ". Is projectile: " + isProjectile(originalSource) + " with damage: " + event.getAmount();
        });

        boolean damageDealt = true;

        if(isIndirect(originalSource)) {
            isBeingDamaged = false;
            damageDealt = handleIndirectDamage(event, target, originalSource);
        } else if (isProjectile(originalSource)) {
            isBeingDamaged = false;
            damageDealt = handleIndirectDamage(event, target, originalSource);
            if(shouldHandleOwnerAttributes(originalSource)){
                handleDirectDamage(event, originalSource, target);
                event.setCanceled(true);
            } else {
                event.setCanceled(true);
            }
        } else if (originalSource.getDirectEntity() instanceof LivingEntity || !isIndirect(originalSource)){
            damageDealt = handleDirectDamage(event, originalSource, target);
        }
        isBeingDamaged = false;
        event.setCanceled(damageDealt);
    }

    private static boolean isProjectile(DamageSource source) {
        return source.getDirectEntity() instanceof AbstractArrow || source.getDirectEntity() instanceof AbstractHurtingProjectile || source.getDirectEntity() instanceof Projectile;
    }

    private static boolean handleDirectDamage(LivingHurtEvent event, DamageSource originalSource, LivingEntity target) {
        LivingEntity attacker = getAttacker(originalSource);
        if (attacker == null) {
            Alembic.printInDebug(() -> "Attacker is null, skipping. Damage source: " + originalSource);
            isBeingDamaged = false;
            return false;
        }
        Multimap<Attribute, AttributeModifier> enchantmentMap = ArrayListMultimap.create();
        // Add attribute modifiers if enchantments exist
        if (handleEnchantments(attacker, target, enchantmentMap)) return false;
        float totalDamage = event.getAmount();
        // if the attacker is a projectile and owner attribute projectiles is on, get the damage type
        float damageOffset;
        if (attacker instanceof Player pl) {
            damageOffset = handlePlayerDamage(target, pl, totalDamage, originalSource);
        } else {
            damageOffset = handleLivingEntityDamage(target, attacker, totalDamage, originalSource);
        }
        totalDamage -= damageOffset;
        attacker.getAttributes().attributes.forEach((attribute, attributeInstance) -> {
            // Remove these if they exist. Existence check is in the called method
            if (attributeInstance.getModifier(TEMP_MOD_UUID) != null) {
                attributeInstance.removeModifier(TEMP_MOD_UUID);
            }
            if (attributeInstance.getModifier(TEMP_MOD_UUID2) != null) {
                attributeInstance.removeModifier(TEMP_MOD_UUID2);
            }
        });
        return true;
    }

    private static float handleLivingEntityDamage(LivingEntity target, LivingEntity attacker, float originalDamage, DamageSource originalSource) {
        AlembicEntityStats targetStats = EntityStatsManager.get(target.getType());
        AlembicEntityStats attackerStats = EntityStatsManager.get(attacker.getType());
        if(attackerStats == null) return 0;
        MutableFloat total = new MutableFloat();
        Reference2FloatMap<AlembicDamageType> damageMap = attackerStats.getDamage();
        Reference2FloatMap<AlembicDamageType> finalTypedDamage = new Reference2FloatOpenHashMap<>();
        damageMap.forEach((damageType, multiplier) -> {
            AttributeInstance attribute = target.getAttribute(damageType.getAttribute());
            if (attribute == null) return;
            float damage = (float) ((originalDamage * multiplier) + attribute.getValue());
            if (damage < 0) {
                Alembic.LOGGER.warn("Damage overrides are too high! Damage was reduced to 0 for " + damageType.getId().toString());
                return;
            }
            finalTypedDamage.put(damageType, damage);
        });
        finalTypedDamage.forEach((damageType, damage) -> {
            float resistanceModifier = getResistanceForType(damageType, target, targetStats).secondFloat();
            if (resistanceModifier < 1) {
                resistanceModifier = 1 + (1 - resistanceModifier);
            } else {
                resistanceModifier = 1 - (resistanceModifier - 1);
            }
            float reducedDamage = damage * (resistanceModifier * damageType.getResistanceIgnore());
            if (damage < 0) {
                Alembic.LOGGER.warn("Damage overrides are too high! Damage was reduced to 0 for {}", damageType.getId().toString());
                return;
            }
            total.add(reducedDamage);
            handleResistances(target, reducedDamage, damageType, originalSource);
        });
        return total.floatValue();
    }

    private static float handlePlayerDamage(LivingEntity target, Player attacker, float originalDamage, DamageSource originalSource) {
        float totalTypedDamage = 0f;
        AlembicEntityStats targetStats = EntityStatsManager.get(target.getType());
        AlembicOverride potentialOverride = OverrideManager.getOverridesForSource(originalSource);
        Alembic.printInDebug(() -> "Original source has override?" + (potentialOverride != null));
        if(potentialOverride != null) {
            Alembic.printInDebug(() -> "Handling override for " + originalSource);
            handleDirectTypedDamage(target, originalDamage, potentialOverride, originalSource);
        }
        for (AlembicDamageType damageType : DamageTypeManager.getDamageTypes()) {
            Alembic.printInDebug(() -> "Handling damage type: " + damageType.getId() + "for player " + attacker.getDisplayName().getString());
            if (!attacker.getAttributes().hasAttribute(damageType.getAttribute())) continue;
            Alembic.printInDebug(() -> "Attacker has attribute: " + damageType.getAttribute().getDescriptionId());
            float damageAttributeValue = (float) attacker.getAttributeValue(damageType.getAttribute());
            if (damageAttributeValue > 0) {
                totalTypedDamage += damageAttributeValue;
                float targetResistance = 0f;
                if (target.getAttributes().hasAttribute(damageType.getResistanceAttribute())) {
                    targetResistance = (float) target.getAttributeValue(damageType.getResistanceAttribute());
                }
                damageAttributeValue *= attacker.getAttackStrengthScale(0.5f);
                float resMod = getResistanceForType(damageType, target, targetStats).secondFloat();
                if (resMod <= 1) { // TODO: This sequence is done in three different places, once slightly differently
                    resMod = 1 + (1 - resMod);
                } else {
                    resMod = 1 - ((resMod - 1) * damageType.getResistanceIgnore());
                }
                damageAttributeValue *= resMod; // TODO: Resistance factored in before event fired, which can modify resistance?
                AlembicDamageEvent.Pre preEvent = new AlembicDamageEvent.Pre(target, attacker, damageType, damageAttributeValue, targetResistance);
                MinecraftForge.EVENT_BUS.post(preEvent);
                float damage = preEvent.getDamage();
                targetResistance = preEvent.getResistance();
                if (damage <= 0 || preEvent.isCanceled()) continue;
                Alembic.printInDebug(() -> "Dealing " + damage + " " + damageType.getId().toString() + " damage to " + target);
                handleResistances(target, damage, damageType, originalSource);
                AlembicDamageEvent.Post postEvent = new AlembicDamageEvent.Post(target, attacker, damageType, damage, targetResistance);
                MinecraftForge.EVENT_BUS.post(postEvent);
            }
        }
        return totalTypedDamage; // TODO: This is only the sum of each damage type's attribute value
    }

    private static boolean handleIndirectDamage(LivingHurtEvent event, LivingEntity target, DamageSource originalSource) {
        float totalDamage = event.getAmount();
        AlembicOverride override = OverrideManager.getOverridesForSource(originalSource);
        Alembic.printInDebug(() -> "Found override for " + originalSource + " with damage" + totalDamage + ": " + override);
        if (override != null) {
            handleIndirectTypedDamage(target, totalDamage, override, originalSource);
            isBeingDamaged = false;
            return true;
        } else {
            isBeingDamaged = false;
            return false;
        }
    }

    private static void handleIndirectTypedDamage(LivingEntity target, float totalDamage, AlembicOverride override, DamageSource originalSource) {
        AlembicEntityStats targetStats = EntityStatsManager.get(target.getType());
        AlembicEntityStats attackerStats = null;
        if (originalSource.getDirectEntity() != null) {
            attackerStats = EntityStatsManager.get(originalSource.getDirectEntity().getType());
        }
        var mapSet = override.getDamagePercents().entrySet();
        if (attackerStats != null) {
            mapSet = attackerStats.getDamage().entrySet();
        }
        for (Map.Entry<AlembicDamageType, Float> entry : mapSet) {
            AlembicDamageType damageType = entry.getKey();
            float percent = entry.getValue();
            float damage = totalDamage * percent;
            if (targetStats != null) {
                float resistanceModifier = getResistanceForType(damageType, target, targetStats).secondFloat();
                if (resistanceModifier < 1) {
                    resistanceModifier = 1 + (1 - resistanceModifier);
                } else {
                    resistanceModifier = 1 - (resistanceModifier - 1);
                }
                damage *= (resistanceModifier * damageType.getResistanceIgnore());
            }
            if (Alembic.isDebugEnabled()) {
                Alembic.LOGGER.debug("Dealing " + damage + " " + damageType.getId().toString() + " damage to " + target);
            }
            totalDamage -= damage;
            // TODO: Blacklist thing
            if (AlembicConfig.ownerAttributeProjectiles.get() && originalSource.getDirectEntity() != null) {
                if (originalSource.getDirectEntity() instanceof LivingEntity owner) {
                    if (owner.getAttribute(damageType.getAttribute()) != null) {
                        double attributeValue = owner.getAttributeValue(damageType.getAttribute());
                        Alembic.printInDebug(() -> "Owner has " + attributeValue + " " + damageType.getAttribute().getDescriptionId());
                        damage += (float) attributeValue;
                    }
                }
            }
            if (damage <= 0) {
                Alembic.LOGGER.warn("Damage overrides are too high! Damage was reduced to 0 for {}", damageType.getId().toString());
            }
            handleResistances(target, damage, damageType, originalSource);
        }
    }

    private static void handleDirectTypedDamage(LivingEntity target, float totalDamage, AlembicOverride override, DamageSource originalSource) {
        for (Object2FloatMap.Entry<AlembicDamageType> entry : override.getDamagePercents().object2FloatEntrySet()) {
            AlembicDamageType damageType = entry.getKey();
            float percent = entry.getFloatValue();
            float damage = totalDamage * percent;
            if (Alembic.isDebugEnabled()) {
                Alembic.LOGGER.debug("Dealing " + damage + " " + damageType.getId().toString() + " damage to " + target);
            }
            totalDamage -= damage;
            if (damage <= 0) {
                Alembic.LOGGER.warn("Damage overrides are too high! Damage was reduced to 0 for " + damageType.getId().toString());
            }
            handleResistances(target, damage, damageType, originalSource);
        }
    }


    private static void handleResistances(LivingEntity target, float totalDamage, AlembicDamageType damageType, DamageSource originalSource) {
        float attributeValue = 0;

        if (target.getAttributes().hasAttribute(damageType.getResistanceAttribute())) { // TODO: resistance handled again after being handled in most other methods
            attributeValue = (float) target.getAttributeValue(damageType.getResistanceAttribute()) * damageType.getResistanceIgnore();
        }

        LivingEntity attacker = null;
        if (originalSource.getDirectEntity() instanceof LivingEntity livingEntity) {
            attacker = livingEntity;
        }

        AlembicDamageEvent.Pre preEvent = new AlembicDamageEvent.Pre(target, attacker, damageType, totalDamage, attributeValue); // TODO: Should only be fired once
        MinecraftForge.EVENT_BUS.post(preEvent);

        totalDamage = preEvent.getDamage();
        if (totalDamage <= 0 || preEvent.isCanceled()) return;

        target.hurtArmor(originalSource, totalDamage);

        totalDamage = CombatRules.getDamageAfterAbsorb(totalDamage, attributeValue, (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        totalDamage = AlembicDamageHelper.getDamageAfterAttributeAbsorb(target, damageType, totalDamage);

        totalDamage = computePotionResistance(totalDamage, target, damageType);

        boolean enchantReduction = damageType.hasEnchantReduction();
        if (enchantReduction && attacker != null) {
            int prot = EnchantmentHelper.getDamageProtection(target.getArmorSlots(), target.level().damageSources().mobAttack(attacker));
            if (prot > 0) {
                totalDamage = CombatRules.getDamageAfterMagicAbsorb(totalDamage, prot);
            }
        }

        /*
        float absorptionValue = target.getAttributes().hasAttribute(alembicDamageType.getAbsorptionAttribute()) ? (float) target.getAttribute(alembicDamageType.getAbsorptionAttribute()).getValue() : 0;
        if (absorptionValue > 0) {
            float absorption = Math.min(absorptionValue, damage);
            absorptionValue -= absorption;
            absorptionValue = Math.max(0, absorptionValue);
            damage -= absorption;
            target.getAttribute(alembicDamageType.getAbsorptionAttribute()).setBaseValue(absorptionValue);
        }
         */

        actuallyHurt(target, damageType, totalDamage, originalSource);

        AlembicDamageEvent.Post postEvent = new AlembicDamageEvent.Post(target, attacker, damageType, totalDamage, attributeValue);
        MinecraftForge.EVENT_BUS.post(postEvent);
    }

    private static float computePotionResistance(float totalDamage, LivingEntity entity, AlembicDamageType damageType) {
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(damageType.getId().withSuffix("_resistance"));
        if (effect != null) {
            MobEffectInstance instance = entity.getEffect(effect);
            if (instance != null) {
                int amplifier = instance.getAmplifier();
                // Find t * (a * 0.1), correlating to the potion resistance amount
                totalDamage -= totalDamage * (((float)amplifier) * 0.1f);
            }
        }

        return totalDamage;
    }

    private static void actuallyHurt(LivingEntity target, AlembicDamageType damageType, float damage, DamageSource originalSource) {
        damage = Math.round(Math.max(damage, 0) * 10) / 10f;
        if (damage > 0) {
            float finalDamage = damage;
            float d = finalDamage;
            Alembic.printInDebug(() -> "Dealing " + d + " " + damageType.getId().toString() + " damage to " + target);
            ComposedData data = ComposedData.createEmpty()
                    .add(ComposedDataTypes.SERVER_LEVEL, (ServerLevel) target.level())
                    .add(ComposedDataTypes.TARGET_ENTITY, target)
                    .add(ComposedDataTypes.FINAL_DAMAGE, d)
                    .add(ComposedDataTypes.ORIGINAL_SOURCE, originalSource)
                    .add(ComposedDataTypes.DAMAGE_TYPE, damageType);
            AlembicDamageDataModificationEvent event = new AlembicDamageDataModificationEvent(data);
            MinecraftForge.EVENT_BUS.post(event);
            damageType.getTags().forEach(tag -> {
                if (tag.testConditions(data)) {
                    Alembic.printInDebug(() -> "Handling tag: " + AlembicTagRegistry.getRegisteredName(tag.getType()));
                    tag.onDamage(data);
                }
            });
            int invulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            float afterAbsorb = Math.max(finalDamage - target.getAbsorptionAmount(), 0.0f);
            target.setAbsorptionAmount(target.getAbsorptionAmount() - (finalDamage - afterAbsorb));
            float absorbedDamage = finalDamage - afterAbsorb;
            if (absorbedDamage > 0.0f && absorbedDamage < 3.4028235E37F && target instanceof Player) {
                ((Player) target).awardStat(Stats.CUSTOM.get(Stats.DAMAGE_DEALT_ABSORBED), Math.round(absorbedDamage * 10.0F));
            }
            float health = target.getHealth();
            if(AlembicConfig.compatDamageEvent.get()) {
                finalDamage = net.minecraftforge.common.ForgeHooks.onLivingDamage(target, originalSource, finalDamage);
            }
            target.getCombatTracker().recordDamage(originalSource, finalDamage);
            target.setHealth(health - finalDamage);
            target.setAbsorptionAmount(target.getAbsorptionAmount() - absorbedDamage);
            sendDamagePacket(target, damageType, finalDamage);
            target.gameEvent(GameEvent.ENTITY_DAMAGE);
            target.invulnerableTime = invulnerableTime;
            Alembic.printInDebug(() -> "Dealt " + d + " " + damageType.getId().toString() + " damage to " + target);
        }
    }

    private static boolean isIndirect(DamageSource source) {
        return source.getDirectEntity() == null;
    }

    private static boolean shouldHandleOwnerAttributes(DamageSource source) {
        if (AlembicConfig.ownerAttributeProjectiles.get()) {
            return source.getDirectEntity() instanceof AbstractArrow || source.getDirectEntity() instanceof AbstractHurtingProjectile || source.getDirectEntity() instanceof Projectile;
        }
        return false;
    }

    private static void sendDamagePacket(LivingEntity target, AlembicDamageType damageType, float damage) {
        AlembicPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> target),
                new ClientboundAlembicDamagePacket(target.getId(), damageType.getId().toString(), damage, damageType.getColor()));
    }

    private static boolean handleEnchantments(LivingEntity attacker, LivingEntity target, Multimap<Attribute, AttributeModifier> map) {
        for (Map.Entry<Enchantment, Integer> entry : attacker.getItemInHand(InteractionHand.MAIN_HAND).getAllEnchantments().entrySet()) {
            if (entry.getKey().equals(Enchantments.BANE_OF_ARTHROPODS)) {
                if (target.getMobType() == MobType.ARTHROPOD) {
                    Attribute alchDamage = DamageTypeManager.getDamageType("arcane_damage").getAttribute();
                    if (alchDamage == null) return true;
                    AttributeModifier attributeModifier = new AttributeModifier(TEMP_MOD_UUID, "Bane of Arthropods", entry.getValue(), AttributeModifier.Operation.ADDITION);
                    attacker.getAttribute(alchDamage).addTransientModifier(attributeModifier);
                    map.put(alchDamage, attributeModifier);
                }
            } else if (entry.getKey().equals(Enchantments.SMITE)) {
                if (target.getMobType() == MobType.UNDEAD) {
                    Attribute alchDamage = DamageTypeManager.getDamageType("arcane_damage").getAttribute();
                    if (alchDamage == null) return true;
                    AttributeModifier attributeModifier = new AttributeModifier(TEMP_MOD_UUID2, "Smite", entry.getValue(), AttributeModifier.Operation.ADDITION);
                    attacker.getAttribute(alchDamage).addTransientModifier(attributeModifier);
                    map.put(alchDamage, attributeModifier);
                }
            } else if (entry.getKey().equals(Enchantments.IMPALING)) {
                if (target.isInWaterOrRain()) {
                    Attribute alchDamage = DamageTypeManager.getDamageType("arcane_damage").getAttribute();
                    if (alchDamage == null) return true;
                    AttributeModifier attributeModifier = new AttributeModifier(TEMP_MOD_UUID, "Impaling", entry.getValue(), AttributeModifier.Operation.ADDITION);
                    attacker.getAttribute(alchDamage).addTransientModifier(attributeModifier);
                    map.put(alchDamage, attributeModifier);
                }
            }
        }
        return false;
    }

    private static LivingEntity getAttacker(DamageSource originalSource) {
        LivingEntity attacker;
        if (originalSource.getDirectEntity() instanceof LivingEntity livingEntity) {
            attacker = livingEntity;
        } else if (originalSource.getDirectEntity() instanceof Projectile projectile) {
            if (projectile.getOwner() instanceof LivingEntity livingEntity) {
                attacker = livingEntity;
            } else {
                attacker = null;
            }
        } else {
            attacker = null;
        }
        return attacker;
    }

    private static DoubleFloatPair getResistanceForType(AlembicDamageType type, LivingEntity entity, @Nullable AlembicEntityStats stats) {
        double resAtt = entity.getAttribute(type.getResistanceAttribute()).getValue();
        float resMod = stats != null ? stats.getResistance(type) : 1f;
        return DoubleFloatPair.of(resAtt, resMod);
    }
}
