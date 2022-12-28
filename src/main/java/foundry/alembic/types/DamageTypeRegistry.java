package foundry.alembic.types;

import foundry.alembic.Alembic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class DamageTypeRegistry {
    private static final List<AlembicDamageType> damageTypes = new ArrayList<>();
    public static final DeferredRegister<Attribute> DAMAGE_ATTRIBUTES = DeferredRegister.create(ForgeRegistries.Keys.ATTRIBUTES, "alembic");
    public static final DeferredRegister<Attribute> DEFENSIVE_ATTRIBUTES = DeferredRegister.create(ForgeRegistries.Keys.ATTRIBUTES, "alembic");
    public static final DeferredRegister<MobEffect> RESISTANCE_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "alembic");

    public static void registerDamageType(AlembicDamageType damageType) {
        damageTypes.add(damageType);
    }

    public static void init() {
        for (AlembicDamageType damageType : damageTypes) {
            DAMAGE_ATTRIBUTES.register(damageType.getId().getPath(), damageType::getAttribute);
            DEFENSIVE_ATTRIBUTES.register(damageType.getId().getPath() + "_shield", damageType::getShieldAttribute);
            DEFENSIVE_ATTRIBUTES.register(damageType.getId().getPath() + "_resistance", damageType::getResistanceAttribute);
            DEFENSIVE_ATTRIBUTES.register(damageType.getId().getPath() + "_absorption", damageType::getAbsorptionAttribute);
        }
    }

    public static List<AlembicDamageType> getDamageTypes() {
        return damageTypes;
    }

    public static AlembicDamageType getDamageType(ResourceLocation id) {
        return damageTypes.stream().filter(damageType -> damageType.getId().equals(id)).findFirst().orElse(null);
    }

    public static void removeDamageType(ResourceLocation id) {
        damageTypes.removeIf(damageType -> damageType.getId().equals(id));
    }

    public static AlembicDamageType getDamageType(DamageSource damageSource) {
        return damageTypes.stream().filter(damageType -> damageType.getDamageSource().equals(damageSource)).findFirst().orElse(null);
    }

    public static AlembicDamageType getDamageType(Attribute attribute) {
        return damageTypes.stream().filter(damageType -> damageType.getAttribute().equals(attribute)).findFirst().orElse(null);
    }

    public static AlembicDamageType getDamageType(String id) {
        return damageTypes.stream().filter(damageType -> damageType.getId().toString().equals(id)).findFirst().orElse(null);
    }

    public static boolean doesDamageTypeExist(ResourceLocation id) {
        return damageTypes.stream().anyMatch(damageType -> damageType.getId().equals(id));
    }

}
