package foundry.alembic.mobeffects;

import foundry.alembic.damagesource.DamageSourceIdentifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.effectslib.api.EffectOverlayRenderer;
import net.tslat.effectslib.api.ExtendedMobEffect;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ImmunityMobEffect extends ExtendedMobEffect {
    private final Set<DamageSourceIdentifier> immunities;

    public ImmunityMobEffect(MobEffectCategory category, int color, Set<DamageSourceIdentifier> immunities) {
        super(category, color);
        this.immunities = immunities;
    }

    @Override
    public boolean beforeIncomingAttack(LivingEntity entity, MobEffectInstance effectInstance, DamageSource source, float amount) {
        return !immunities.contains(DamageSourceIdentifier.create(source.getMsgId())) && super.beforeIncomingAttack(entity, effectInstance, source, amount);
    }

    @Override
    public @Nullable EffectOverlayRenderer getOverlayRenderer() {
        return super.getOverlayRenderer();
    }
}
