package foundry.alembic.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;

public class Utils {
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
