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
    public static final DeferredRegister<MobEffect> RESISTANCE_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Alembic.MODID);

    public static void registerDamageType(ResourceLocation id, AlembicDamageType damageType) {
        DAMAGE_TYPES.put(id, damageType);
    }

    public static void init() {
    }

    public static Collection<AlembicDamageType> getDamageTypes() {
        return Collections.unmodifiableCollection(DAMAGE_TYPES.values());
    }

    public static AlembicDamageType getDamageType(ResourceLocation id) {
        return DAMAGE_TYPES.get(id);
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
