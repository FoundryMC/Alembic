package foundry.alembic.types.potion;

import foundry.alembic.caps.AlembicFlammableHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tslat.effectslib.api.ExtendedMobEffect;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AlembicPotionRegistry {
    public static final Map<ResourceLocation, AlembicPotionDataHolder> IMMUNITY_DATA = new HashMap<>();
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "alembic");

    public static final RegistryObject<MobEffect> FIRE = MOB_EFFECTS.register("fire", FireMobEffect::new);
    public static final RegistryObject<MobEffect> FROSTBITE = MOB_EFFECTS.register("frostbite", FrostbiteMobEffect::new);
    public static final RegistryObject<MobEffect> SOUL_FIRE = MOB_EFFECTS.register("soul_fire", SoulFireMobEffect::new);

    public static class SoulFireMobEffect extends ExtendedMobEffect {
        public SoulFireMobEffect() {
            super(MobEffectCategory.HARMFUL, 0x9760FB);
        }

        @Override
        public void onApplication(@Nullable MobEffectInstance effectInstance, @Nullable Entity source, LivingEntity entity, int amplifier) {
            if (effectInstance== null) return;
            entity.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent(cap -> cap.setFireType("soul"));
            if (!entity.isOnFire()) {
                entity.setRemainingFireTicks(effectInstance.getDuration());
            } else if (entity.getRemainingFireTicks() < effectInstance.getDuration()) {
                entity.setRemainingFireTicks(effectInstance.getDuration());
            }
        }
        @Override
        public boolean shouldTickEffect(@Nullable MobEffectInstance effectInstance, @Nullable LivingEntity entity, int ticksRemaining, int amplifier) {
            return true;
        }

        @Override
        public void tick(LivingEntity entity, @Nullable MobEffectInstance effectInstance, int amplifier) {
            if (effectInstance== null) return;
            if (!entity.isOnFire()) {
                entity.setRemainingFireTicks(effectInstance.getDuration());
            } else if (entity.getRemainingFireTicks() < effectInstance.getDuration()) {
                entity.setRemainingFireTicks(effectInstance.getDuration());
            }
        }
    }

    public static class FrostbiteMobEffect extends ExtendedMobEffect {
        public FrostbiteMobEffect() {
            super(MobEffectCategory.HARMFUL, 0x00F1F1);
        }

        @Override
        public void onApplication(@Nullable MobEffectInstance effectInstance, @Nullable Entity source, LivingEntity entity, int amplifier) {
            if (effectInstance== null) return;
            if (entity.getTicksFrozen() < effectInstance.getDuration()) {
                entity.setTicksFrozen(effectInstance.getDuration());
            }
        }
        @Override
        public boolean shouldTickEffect(@Nullable MobEffectInstance effectInstance, @Nullable LivingEntity entity, int ticksRemaining, int amplifier) {
            return true;
        }

        @Override
        public void tick(LivingEntity entity, @Nullable MobEffectInstance effectInstance, int amplifier) {
            if (effectInstance== null) return;
            if (entity.getTicksFrozen() < effectInstance.getDuration()) {
                entity.setTicksFrozen(effectInstance.getDuration());
            }
        }
    }

    public static class FireMobEffect extends ExtendedMobEffect {
        public FireMobEffect() {
            super(MobEffectCategory.HARMFUL, 0xF14700);
        }

        @Override
        public void onApplication(@Nullable MobEffectInstance effectInstance, @Nullable Entity source, LivingEntity entity, int amplifier) {
            if (effectInstance== null) return;
            entity.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent(cap -> cap.setFireType("normal"));
            if (!entity.isOnFire()) {
                entity.setRemainingFireTicks(effectInstance.getDuration());
            } else if (entity.getRemainingFireTicks() < effectInstance.getDuration()) {
                entity.setRemainingFireTicks(effectInstance.getDuration());
            }
        }

        @Override
        public boolean shouldTickEffect(@Nullable MobEffectInstance effectInstance, @Nullable LivingEntity entity, int ticksRemaining, int amplifier) {
            return true;
        }

        @Override
        public void tick(LivingEntity entity, @Nullable MobEffectInstance effectInstance, int amplifier) {
            if (effectInstance== null) return;
            if (!entity.isOnFire()) {
                entity.setRemainingFireTicks(effectInstance.getDuration());
            } else if (entity.getRemainingFireTicks() < effectInstance.getDuration()) {
                entity.setRemainingFireTicks(effectInstance.getDuration());
            }
        }
    }
}
