package foundry.alembic.types;

import foundry.alembic.Alembic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class DamageTypeRegistry {
    private static final Map<ResourceLocation, AlembicDamageType> DAMAGE_TYPES = new HashMap<>();
    public static final DeferredRegister<Attribute> DAMAGE_ATTRIBUTES = DeferredRegister.create(ForgeRegistries.Keys.ATTRIBUTES, Alembic.MODID);
    public static final DeferredRegister<Attribute> DEFENSIVE_ATTRIBUTES = DeferredRegister.create(ForgeRegistries.Keys.ATTRIBUTES, Alembic.MODID);
    public static final DeferredRegister<MobEffect> RESISTANCE_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Alembic.MODID);

    public static void registerDamageType(ResourceLocation id, AlembicDamageType damageType) {
        if (DAMAGE_TYPES.putIfAbsent(id, damageType) != null) {
            Alembic.LOGGER.error("DamageType with id: '%s' already present. Failed to register new value.".formatted(id));
        }
    }

    public static void init() {
        for (Map.Entry<ResourceLocation, AlembicDamageType> entry : DAMAGE_TYPES.entrySet()) {
            DAMAGE_ATTRIBUTES.register(entry.getKey().getPath(), entry.getValue()::getAttribute);
            DEFENSIVE_ATTRIBUTES.register(AlembicTypeModifier.SHIELDING.getId(entry.getValue()), entry.getValue()::getShieldAttribute);
            if (!ForgeRegistries.ATTRIBUTES.containsValue(entry.getValue().getResistanceAttribute())) {
                DEFENSIVE_ATTRIBUTES.register(AlembicTypeModifier.RESISTANCE.getId(entry.getValue()), entry.getValue()::getResistanceAttribute);
            }
            DEFENSIVE_ATTRIBUTES.register(AlembicTypeModifier.ABSORPTION.getId(entry.getValue()), entry.getValue()::getAbsorptionAttribute);
        }
    }

    static void replaceWithData(AlembicDamageType damageType) {
        DAMAGE_TYPES.get(damageType.getId()).copyValues(damageType);
    }

    public static Collection<AlembicDamageType> getDamageTypes() {
        return Collections.unmodifiableCollection(DAMAGE_TYPES.values());
    }

    public static AlembicDamageType getDamageType(ResourceLocation id) {
        return DAMAGE_TYPES.get(id);
    }

    public static AlembicDamageType getDamageType(DamageSource damageSource) {
        return DAMAGE_TYPES.values().stream().filter(damageType -> damageType.getDamageSource().equals(damageSource)).findFirst().orElse(null);
    }

    public static AlembicDamageType getDamageType(Attribute attribute) {
        return DAMAGE_TYPES.values().stream().filter(damageType -> damageType.getAttribute().equals(attribute)).findFirst().orElse(null);
    }

    public static AlembicDamageType getDamageType(String id) {
        return DAMAGE_TYPES.get(id.contains(":") ? new ResourceLocation(id) : Alembic.location(id));
    }

    public static boolean doesDamageTypeExist(ResourceLocation id) {
        return DAMAGE_TYPES.containsKey(id);
    }

}
