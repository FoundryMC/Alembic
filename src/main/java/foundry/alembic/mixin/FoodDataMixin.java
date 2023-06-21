package foundry.alembic.mixin;

import foundry.alembic.event.AlembicFoodChangeEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FoodData.class)
public class FoodDataMixin {

    @Shadow private int foodLevel;

    @Shadow private int lastFoodLevel;

    @Shadow private float saturationLevel;

    @Shadow private float exhaustionLevel;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 0))
    private void alembic$onTick(Player pPlayer, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new AlembicFoodChangeEvent.Decrease(pPlayer, foodLevel, lastFoodLevel, saturationLevel, exhaustionLevel));
    }

    @Inject(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V", shift = At.Shift.AFTER))
    private void alembic$onEat(Item p_38713_, ItemStack p_38714_, LivingEntity entity, CallbackInfo ci) {
        if (entity instanceof Player pl) {
            MinecraftForge.EVENT_BUS.post(new AlembicFoodChangeEvent.Increase(pl, foodLevel, lastFoodLevel, saturationLevel, exhaustionLevel));
        }
    }
}
