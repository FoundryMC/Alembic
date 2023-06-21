package foundry.alembic.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import foundry.alembic.AlembicAPI;
import foundry.alembic.caps.AlembicFlammableHandler;
import foundry.alembic.networking.AlembicPacketHandler;
import foundry.alembic.potion.OverrideHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin{

    @Inject(method = "setRemainingFireTicks", at = @At("HEAD"))
    private void alembic$setRemainingFireTicks(int ticks, CallbackInfo info) {
        Entity entity = (Entity) (Object) this;
        if(entity instanceof LivingEntity le){
            if (ticks > 0) {
                OverrideHelper.addFireEffect(ticks, le);
            } else {
                OverrideHelper.removeFireEffect(le);
            }
        }
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void alembic$baseTick(CallbackInfo info) {
//        Entity entity = (Entity) (Object) this;
//        if(!entity.isOnFire()) {
//            entity.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent(cap -> {
//                cap.setFireType("normal");
//                AlembicPacketHandler.sendFirePacket(entity, "normal");
//            });
//        }
    }



    @WrapOperation(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 0))
    private boolean alembic$onEntityBaseTick(Entity instance, DamageSource pSource, float pAmount, Operation<Boolean> original) {
        if(instance.getCapability(AlembicFlammableHandler.CAPABILITY, null).map(cap -> cap.getFireType().equals("soul")).orElse(false)){
            return instance.hurt(AlembicAPI.SOUL_FIRE, pAmount);
        } else {
            return original.call(instance, pSource, pAmount);
        }
    }
}
