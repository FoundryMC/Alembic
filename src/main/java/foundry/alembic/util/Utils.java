package foundry.alembic.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;

public class Utils {
    public static final Gson GSON = new GsonBuilder().setLenient().create();

    public static ResourceLocation sanitize(ResourceLocation rl, String... toRemove) {
        String sanitized = rl.getPath();
        for (String remove : toRemove) {
            sanitized = sanitized.replace(remove, "");
        }
        return new ResourceLocation(rl.getNamespace(), sanitized);
    }

    public static EquipmentSlot equipmentFromHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }
}
