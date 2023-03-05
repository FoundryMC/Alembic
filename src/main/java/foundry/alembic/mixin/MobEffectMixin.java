package foundry.alembic.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import foundry.alembic.AlembicAPI;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobEffect.class)
public class MobEffectMixin {


    @WrapOperation(method = "applyEffectTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 0))
    private boolean poisonHurt(LivingEntity livingEntity, net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        return livingEntity.hurt(AlembicAPI.ALCHEMICAL, amount);
    }

    @WrapOperation(method = "applyEffectTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 2))
    private boolean harmingHurt(LivingEntity livingEntity, net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        return livingEntity.hurt(AlembicAPI.ALCHEMICAL, amount);
    }

    @WrapOperation(method = "applyInstantenousEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 0))
    private boolean poisonInstHurt(LivingEntity livingEntity, net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        return livingEntity.hurt(AlembicAPI.ALCHEMICAL, amount);
    }

    @WrapOperation(method = "applyInstantenousEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 1))
    private boolean harmingInstHurt(LivingEntity livingEntity, net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        return livingEntity.hurt(AlembicAPI.ALCHEMICAL, amount);
    }
}
