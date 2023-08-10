package foundry.alembic.potion;

import foundry.alembic.caps.AlembicFlammable;
import foundry.alembic.mobeffects.FireMobEffect;
import foundry.alembic.mobeffects.FrostbiteMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AlembicMobEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "alembic");

    public static final RegistryObject<MobEffect> FIRE = MOB_EFFECTS.register("fire", () -> new FireMobEffect(MobEffectCategory.HARMFUL, 0xF14700, AlembicFlammable.NORMAL_FIRE));
    public static final RegistryObject<MobEffect> FROSTBITE = MOB_EFFECTS.register("frostbite", FrostbiteMobEffect::new);
    public static final RegistryObject<MobEffect> SOUL_FIRE = MOB_EFFECTS.register("soul_fire", () -> new FireMobEffect(MobEffectCategory.HARMFUL, 0x9760FB, AlembicFlammable.SOUL_FIRE));
}
