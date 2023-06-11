package foundry.alembic.mixin;

import foundry.alembic.AlembicAPI;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EvokerFangs.class)
public class EvokerFangsMixin {

    @Redirect(method = "dealDamageTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 0))
    private boolean alembic$evokerFangsDamage(LivingEntity entity, DamageSource source, float amount) {
        return entity.hurt(AlembicAPI.evokerFangs(entity), amount);
    }
}
