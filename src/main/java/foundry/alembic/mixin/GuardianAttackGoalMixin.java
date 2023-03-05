package foundry.alembic.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import foundry.alembic.AlembicAPI;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.entity.monster.Guardian$GuardianAttackGoal")
public class GuardianAttackGoalMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 0))
    private boolean alembic$guardianBeamDamage(LivingEntity entity, DamageSource source, float amount) {
        return entity.hurt(AlembicAPI.GUARDIAN_BEAM, amount);
    }
}
