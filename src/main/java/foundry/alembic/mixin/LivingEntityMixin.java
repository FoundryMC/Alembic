package foundry.alembic.mixin;

import foundry.alembic.mobeffect.AlembicMobEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "tryAddFrost", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getAttribute(Lnet/minecraft/world/entity/ai/attributes/Attribute;)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;"))
    private void tryAddFrost(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.isFullyFrozen() && entity instanceof Player player) {
                player.addEffect(new MobEffectInstance(AlembicMobEffectRegistry.FROSTBITE.get(), entity.getTicksFrozen(), 0));
        }
    }
}
