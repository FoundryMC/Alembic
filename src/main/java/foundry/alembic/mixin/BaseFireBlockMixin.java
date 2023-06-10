package foundry.alembic.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import foundry.alembic.AlembicAPI;
import foundry.alembic.caps.AlembicFlammableHandler;
import foundry.alembic.networking.AlembicPacketHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {

    @WrapOperation(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean alembic$onEntityInside(Entity instance, DamageSource pSource, float pAmount, Operation<Boolean> original, BlockState pState) {
        if (pState.is(Blocks.SOUL_FIRE)) {
            AlembicPacketHandler.sendFirePacket(instance, "soul");
            instance.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent(cap -> cap.setFireType("soul"));
            return instance.hurt(AlembicAPI.SOUL_FIRE, pAmount);
        } else {
            AlembicPacketHandler.sendFirePacket(instance, "normal");
            instance.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent(cap -> cap.setFireType("normal"));
            return original.call(instance, pSource, pAmount);
        }
    }
}
