package foundry.alembic.mixin;

import foundry.alembic.event.AlembicFoodDecreaseEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FoodData.class)
public class FoodDataMixin {

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onTick(Player pPlayer, CallbackInfo ci, Difficulty difficulty){
        MinecraftForge.EVENT_BUS.post(new AlembicFoodDecreaseEvent(pPlayer.getFoodData().getFoodLevel(), pPlayer.getFoodData().getFoodLevel() - 1, pPlayer.getFoodData().getSaturationLevel(), pPlayer.getFoodData().getExhaustionLevel()));
    }
}
