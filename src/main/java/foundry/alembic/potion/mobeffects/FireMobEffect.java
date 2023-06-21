package foundry.alembic.potion.mobeffects;

import foundry.alembic.caps.AlembicFlammableHandler;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.effectslib.api.ExtendedMobEffect;
import org.jetbrains.annotations.Nullable;

public class FireMobEffect extends ExtendedMobEffect {
    private final String fireType;
    public FireMobEffect(MobEffectCategory category, int color, String fireType) {
        super(category, color);
        this.fireType = fireType;
    }

    @Override
    public void onApplication(@Nullable MobEffectInstance effectInstance, @Nullable Entity source, LivingEntity entity, int amplifier) {
        if (effectInstance == null) return;
        entity.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent(cap -> cap.setFireType(fireType));
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
        if (effectInstance == null) return;
        if (!entity.isOnFire()) {
            entity.setRemainingFireTicks(effectInstance.getDuration());
        } else if (entity.getRemainingFireTicks() < effectInstance.getDuration()) {
            entity.setRemainingFireTicks(effectInstance.getDuration());
        }
    }
}
