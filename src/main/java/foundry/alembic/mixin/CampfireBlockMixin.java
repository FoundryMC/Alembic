package foundry.alembic.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import foundry.alembic.AlembicAPI;
import foundry.alembic.caps.AlembicFlammableHandler;
import foundry.alembic.networking.AlembicPacketHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
    @WrapOperation(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean alembic$onEntityInside(Entity instance, DamageSource pSource, float pAmount, Operation<Boolean> original, BlockState pState) {
        if (pState.is(Blocks.SOUL_CAMPFIRE)) {
            instance.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent(cap -> cap.setFireType("soul"));
            AlembicPacketHandler.sendFirePacket(instance, "soul");
            return instance.hurt(AlembicAPI.soulFire(instance), pAmount);
        } else {
            instance.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent(cap -> cap.setFireType("normal"));
            AlembicPacketHandler.sendFirePacket(instance, "normal");
            return original.call(instance, pSource, pAmount);
        }
    }
}
