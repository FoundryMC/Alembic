package foundry.alembic.damage;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import foundry.alembic.Alembic;
import foundry.alembic.AlembicConfig;
import foundry.alembic.AlembicDamageHelper;
import foundry.alembic.event.AlembicDamageDataModificationEvent;
import foundry.alembic.event.AlembicDamageEvent;
import foundry.alembic.networking.AlembicPacketHandler;
import foundry.alembic.networking.ClientboundAlembicDamagePacket;
import foundry.alembic.override.AlembicOverride;
import foundry.alembic.override.OverrideManager;
import foundry.alembic.resistances.AlembicEntityStats;
import foundry.alembic.resistances.ResistanceManager;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeManager;
import foundry.alembic.types.tag.AlembicTagRegistry;
import foundry.alembic.util.ComposedData;
import foundry.alembic.util.ComposedDataTypes;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
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

import java.util.Map;

import static foundry.alembic.ForgeEvents.TEMP_MOD_UUID;
import static foundry.alembic.ForgeEvents.TEMP_MOD_UUID2;
import static foundry.alembic.ForgeEvents.isBeingDamaged;

public class AlembicDamageHandler {
    public static void handleDamage(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource originalSource = event.getSource();
        if (originalSource.getDirectEntity() instanceof LivingEntity || !isIndirect(originalSource)) {
            LivingEntity attacker = getAttacker(originalSource);
            if (attacker == null) {
                Alembic.printInDebug(() -> "Attacker is null, skipping. Damage source: " + originalSource);
                isBeingDamaged = false;
                return;
            }
            Multimap<Attribute, AttributeModifier> enchantmentMap = ArrayListMultimap.create();
            if (handleEnchantments(attacker, target, enchantmentMap)) return;
            float totalDamage = event.getAmount();
            float damageOffset = 0;
            if (attacker instanceof Player pl) {
                damageOffset = handlePlayerDamage(target, pl, totalDamage, originalSource);
            } else {
                damageOffset = handleLivingEntityDamage(target, attacker, totalDamage, originalSource);
            }
            totalDamage -= damageOffset;
            int time = target.invulnerableTime;
            target.invulnerableTime = 0;
            float fudge = AlembicConfig.enableCompatFudge.get() ? 0.0001f : 0.0f;
            if (totalDamage + fudge >= fudge) {
                target.hurt(entitySource(attacker), totalDamage + fudge);
            }
            target.invulnerableTime = time;
            attacker.getAttributes().attributes.forEach((attribute, attributeInstance) -> {
                if (attributeInstance.getModifier(TEMP_MOD_UUID) != null) {
                    attributeInstance.removeModifier(TEMP_MOD_UUID);
                }
                if (attributeInstance.getModifier(TEMP_MOD_UUID2) != null) {
                    attributeInstance.removeModifier(TEMP_MOD_UUID2);
                }
            });
        } else if (isIndirect(originalSource)) {
            isBeingDamaged = false;
            handleIndirectDamage(event, target, originalSource);
            return;
        }
        isBeingDamaged = false;
        event.setCanceled(true);
    }

    private static float handleLivingEntityDamage(LivingEntity target, LivingEntity attacker, float originalDamage, DamageSource originalSource) {
        AlembicEntityStats targetStats = ResistanceManager.get(target.getType());
        AlembicEntityStats attackerStats = ResistanceManager.get(attacker.getType());
        MutableFloat total = new MutableFloat();
        Object2FloatMap<AlembicDamageType> damageMap = attackerStats.getDamage();
        Object2FloatMap<AlembicDamageType> finalTypedDamage = new Object2FloatOpenHashMap<>();
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
            float resistanceModifier = getResistanceForType(damageType, target, targetStats).getSecond();
            if (resistanceModifier < 1) {
                resistanceModifier = 1 + (1 - resistanceModifier);
            } else {
                resistanceModifier = 1 - (resistanceModifier - 1);
            }
            float reducedDamage = damage * (resistanceModifier * damageType.getResistanceIgnore());
            if (damage < 0) {
                Alembic.LOGGER.warn("Damage overrides are too high! Damage was reduced to 0 for " + damageType.getId().toString());
                return;
            }
            total.add(reducedDamage);
            handleResistances(target, reducedDamage, damageType, originalSource);
        });
        return total.floatValue();
    }

    private static float handlePlayerDamage(LivingEntity target, Player attacker, float originalDamage, DamageSource originalSource) {
        float totalTypedDamage = 0f;
        AlembicEntityStats targetStats = ResistanceManager.get(target.getType());
        for (AlembicDamageType damageType : DamageTypeManager.getDamageTypes()) {
            Alembic.printInDebug(() -> "Handling damage type: " + damageType.getId() + "for player " + attacker.getDisplayName().getString());
            if (!attacker.getAttributes().hasAttribute(damageType.getAttribute())) continue;
            Alembic.printInDebug(() -> "Attacker has attribute: " + damageType.getAttribute().getDescriptionId());
            float damageAttributeValue = (float) attacker.getAttributeValue(damageType.getAttribute());
            if (damageAttributeValue > 0) {
                float targetResistance = 0f;
                if (target.getAttributes().hasAttribute(damageType.getResistanceAttribute())) {
                    targetResistance = (float) target.getAttributeValue(damageType.getResistanceAttribute());
                }
                damageAttributeValue *= attacker.getAttackStrengthScale(0.5f);
                float resMod = getResistanceForType(damageType, target, targetStats).getSecond();
                if (resMod <= 1) {
                    resMod = 1 + (1 - resMod);
                } else {
                    resMod = 1 - ((resMod - 1) * damageType.getResistanceIgnore());
                }
                damageAttributeValue *= resMod;
                AlembicDamageEvent.Pre preEvent = new AlembicDamageEvent.Pre(target, attacker, damageType, damageAttributeValue, targetResistance);
                MinecraftForge.EVENT_BUS.post(preEvent);
                float damage = preEvent.getDamage();
                targetResistance = preEvent.getResistance();
                if (damage <= 0 || preEvent.isCanceled()) continue;
                Alembic.printInDebug(() -> "Dealing " + damage + " " + damageType.getId().toString() + " damage to " + target);
                handleResistances(target, damage, damageType, originalSource);
                AlembicDamageEvent.Post postEvent = new AlembicDamageEvent.Post(target, attacker, damageType, damage, targetResistance);
                MinecraftForge.EVENT_BUS.post(postEvent);
                totalTypedDamage += postEvent.getDamage();
            }
        }
        return totalTypedDamage;
    }

    private static void handleIndirectDamage(LivingHurtEvent event, LivingEntity target, DamageSource originalSource) {
        float totalDamage = event.getAmount();
        AlembicOverride override = OverrideManager.getOverridesForSource(originalSource);
        Alembic.printInDebug(() -> "Found override for " + originalSource + " with damage" + totalDamage + ": " + override);
        if (override != null) {
            handleIndirectTypedDamage(target, totalDamage, override, originalSource);
            isBeingDamaged = false;
            event.setCanceled(true);
        } else {
            isBeingDamaged = false;
        }
    }

    private static void handleIndirectTypedDamage(LivingEntity target, float totalDamage, AlembicOverride override, DamageSource originalSource) {
        for (Object2FloatMap.Entry<AlembicDamageType> entry : override.getDamagePercents().object2FloatEntrySet()) {
            AlembicDamageType damageType = entry.getKey();
            float percent = entry.getFloatValue();
            float damage = totalDamage * percent;
            float finalDamage = damage;
            Alembic.printInDebug(() -> "Dealing " + finalDamage + " " + damageType.getId().toString() + " damage to " + target);
            totalDamage -= damage;
            if (AlembicConfig.ownerAttributeProjectiles.get() && originalSource.getDirectEntity() != null) {
                if (originalSource.getDirectEntity() instanceof LivingEntity owner) {
                    if (owner.getAttribute(damageType.getAttribute()) != null) {
                        double attributeValue = owner.getAttributeValue(damageType.getAttribute());
                        Alembic.printInDebug(() -> "Owner has " + attributeValue + " " + damageType.getAttribute().getDescriptionId());
                        damage += attributeValue;
                    }
                }
            }
            if (damage <= 0) {
                Alembic.LOGGER.warn("Damage overrides are too high! Damage was reduced to 0 for " + damageType.getId().toString());
            }
            handleResistances(target, damage, damageType, originalSource);
        }
    }


    private static void handleResistances(LivingEntity target, float totalDamage, AlembicDamageType damageType, DamageSource originalSource) {
        float attributeValue = 0;
        if (target.getAttributes().hasAttribute(damageType.getResistanceAttribute())) {
            attributeValue = (float) target.getAttributeValue(damageType.getResistanceAttribute()) * damageType.getResistanceIgnore();
        }
        LivingEntity attacker = null;
        if (originalSource.getDirectEntity() instanceof LivingEntity livingEntity) {
            attacker = livingEntity;
        }
        AlembicDamageEvent.Pre preEvent = new AlembicDamageEvent.Pre(target, attacker, damageType, totalDamage, attributeValue);
        MinecraftForge.EVENT_BUS.post(preEvent);
        totalDamage = preEvent.getDamage();
        if (totalDamage <= 0 || preEvent.isCanceled()) return;
        target.hurtArmor(originalSource, totalDamage);
        totalDamage = CombatRules.getDamageAfterAbsorb(totalDamage, attributeValue, (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        totalDamage = AlembicDamageHelper.getDamageAfterAttributeAbsorb(target, damageType, totalDamage);
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

    private static void actuallyHurt(LivingEntity target, AlembicDamageType damageType, float damage, DamageSource originalSource) {
        damage = Math.round(Math.max(damage, 0) * 10) / 10f;
        if (damage > 0) {
            float finalDamage = damage;
            Alembic.printInDebug(() -> "Dealing " + finalDamage + " " + damageType.getId().toString() + " damage to " + target);
            damageType.getTags().forEach(tag -> {
                ComposedData data = ComposedData.createEmpty()
                        .add(ComposedDataTypes.SERVER_LEVEL, (ServerLevel) target.level())
                        .add(ComposedDataTypes.TARGET_ENTITY, target)
                        .add(ComposedDataTypes.FINAL_DAMAGE, finalDamage)
                        .add(ComposedDataTypes.ORIGINAL_SOURCE, originalSource)
                        .add(ComposedDataTypes.DAMAGE_TYPE, damageType);
                AlembicDamageDataModificationEvent event = new AlembicDamageDataModificationEvent(data);
                MinecraftForge.EVENT_BUS.post(event);
                data = event.getData();
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
            target.getCombatTracker().recordDamage(target.level().damageSources().generic(), finalDamage);
            target.setHealth(health - finalDamage);
            target.setAbsorptionAmount(target.getAbsorptionAmount() - absorbedDamage);
            sendDamagePacket(target, damageType, finalDamage);
            target.gameEvent(GameEvent.ENTITY_DAMAGE);
            target.invulnerableTime = invulnerableTime;
            Alembic.printInDebug(() -> "Dealt " + finalDamage + " " + damageType.getId().toString() + " damage to " + target);
        }
    }

    private static boolean isIndirect(DamageSource source) {
        boolean isIndirect = source.getDirectEntity() == null;
        if (!AlembicConfig.ownerAttributeProjectiles.get()) {
            if (source.getDirectEntity() instanceof AbstractArrow || source.getDirectEntity() instanceof AbstractHurtingProjectile || source.getDirectEntity() instanceof Projectile) {
                isIndirect = true;
            }
        }
        return isIndirect;
    }

    private static void sendDamagePacket(LivingEntity target, AlembicDamageType damageType, float damage) {
        AlembicPacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(() ->
                        new PacketDistributor.TargetPoint(target.getX(), target.getY(), target.getZ(), 128, target.level().dimension())),
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

    private static Pair<Double, Float> getResistanceForType(AlembicDamageType type, LivingEntity entity, AlembicEntityStats stats) {
        double resAtt = entity.getAttribute(type.getResistanceAttribute()).getValue();
        float resMod = 0.0f;
        if (entity instanceof Player) {
            resMod = 1.0f;
        }
        if (stats != null) {
            if (stats.getResistances().containsKey(type)) {
                resMod = stats.getResistances().getFloat(type);
            }
        }
        return Pair.of(resAtt, resMod);
    }

    private static DamageSource entitySource(LivingEntity entity) {
        return entity instanceof Player p ? p.level().damageSources().playerAttack(p) : entity.level().damageSources().mobAttack(entity);
    }
}
