package foundry.alembic.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import foundry.alembic.Alembic;
import foundry.alembic.AlembicAPI;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobEffect.class)
public class MobEffectMixin {


    @WrapOperation(method = "applyEffectTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 0))
    private boolean alembic$poisonHurt(LivingEntity livingEntity, net.minecraft.world.damagesource.DamageSource damageSource, float amount, Operation<Boolean> original) {
        Alembic.printInDebug(() -> "Poisoning " + livingEntity);
        return livingEntity.hurt(AlembicAPI.alchemical(livingEntity), amount);
    }

    @WrapOperation(method = "applyEffectTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 2))
    private boolean alembic$harmingHurt(LivingEntity livingEntity, net.minecraft.world.damagesource.DamageSource damageSource, float amount, Operation<Boolean> original) {
        return livingEntity.hurt(AlembicAPI.alchemical(livingEntity), amount);
    }

    @WrapOperation(method = "applyInstantenousEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 0))
    private boolean alembic$poisonInstHurt(LivingEntity livingEntity, net.minecraft.world.damagesource.DamageSource damageSource, float amount, Operation<Boolean> original) {
        return livingEntity.hurt(AlembicAPI.alchemical(livingEntity), amount);
    }

    @WrapOperation(method = "applyInstantenousEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 1))
    private boolean alembic$harmingInstHurt(LivingEntity livingEntity, net.minecraft.world.damagesource.DamageSource damageSource, float amount, Operation<Boolean> original) {
        return livingEntity.hurt(AlembicAPI.alchemical(livingEntity), amount);
    }
}
