package foundry.alembic.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import foundry.alembic.AlembicAPI;
import foundry.alembic.types.potion.AlembicFlammablePlayer;
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
            ((AlembicFlammablePlayer) instance).setAlembicLastFireBlock("soul");
            return instance.hurt(AlembicAPI.SOUL_FIRE, pAmount);
        } else {
            return original.call(instance, pSource, pAmount);
        }
    }
}
