package foundry.alembic.mixin;

import foundry.alembic.AlembicAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {

    @Unique
    BlockState fireState = null;

    @Inject(method = "entityInside", at = @At("HEAD"))
    private void onEntityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, CallbackInfo ci) {
        fireState = pState;
    }

    @Redirect(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean onEntityInside(Entity instance, DamageSource pSource, float pAmount) {
        if(fireState.getBlock() == Blocks.SOUL_FIRE)
            return instance.hurt(AlembicAPI.SOUL_FIRE, pAmount);
        else
            return instance.hurt(pSource, pAmount);
    }
}
