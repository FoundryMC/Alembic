package foundry.alembic.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

public class Utils {
    public static final Gson GSON = new GsonBuilder().setLenient().create();

    public static boolean shouldParse(JsonElement element, ICondition.IContext context) {
        if (element.isJsonNull() || (element.isJsonObject() && element.getAsJsonObject().size() == 0)) {
            return false;
        }
        return CraftingHelper.processConditions(element.getAsJsonObject(), "forge:conditions", context);
    }

    public static EquipmentSlot equipmentFromHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }
}
