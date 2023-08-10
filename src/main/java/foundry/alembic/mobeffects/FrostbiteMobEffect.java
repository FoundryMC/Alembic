package foundry.alembic.mobeffects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.effectslib.api.ExtendedMobEffect;
import org.jetbrains.annotations.Nullable;

public class FrostbiteMobEffect extends ExtendedMobEffect {
    public FrostbiteMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x00F1F1);
    }

    @Override
    public void onApplication(@Nullable MobEffectInstance effectInstance, @Nullable Entity source, LivingEntity entity, int amplifier) {
        if (effectInstance == null) return;
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
        if (effectInstance == null) return;
        if (entity.getTicksFrozen() < effectInstance.getDuration()) {
            entity.setTicksFrozen(effectInstance.getDuration());
        }
    }

    @Override
    public boolean doClientSideEffectTick(MobEffectInstance effectInstance, LivingEntity entity) {
        return true;
    }
}
