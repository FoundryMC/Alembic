package foundry.alembic.mobeffect.mobeffects;

import foundry.alembic.util.TagOrElements;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.effectslib.api.ExtendedMobEffect;

import java.util.Set;
import java.util.stream.Collectors;

public class ImmunityMobEffect extends ExtendedMobEffect {
    private final Set<TagOrElements.Lazy<DamageType>> rawImmunities;
    private Set<DamageType> immunities;

    public ImmunityMobEffect(MobEffectCategory category, int color, Set<TagOrElements.Lazy<DamageType>> rawImmunities) {
        super(category, color);
        this.rawImmunities = rawImmunities;
    }

    @Override
    public boolean beforeIncomingAttack(LivingEntity entity, MobEffectInstance effectInstance, DamageSource source, float amount) {
        if (immunities == null) {
            immunities = rawImmunities.stream().flatMap(damageTypeLazy -> damageTypeLazy.getElements(entity.level().registryAccess()).stream().map(Holder::get)).collect(Collectors.toSet());
        }
        return !immunities.contains(source.type()) && super.beforeIncomingAttack(entity, effectInstance, source, amount);
    }
}
