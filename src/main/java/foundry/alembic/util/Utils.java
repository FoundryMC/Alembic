package foundry.alembic.util;

import net.minecraft.resources.ResourceLocation;

public class Utils {
    public static ResourceLocation sanitize(ResourceLocation rl, String... toRemove) {
        String sanitized = rl.getPath();
        for (String remove : toRemove) {
            sanitized = sanitized.replace(remove, "");
        }
        return new ResourceLocation(rl.getNamespace(), sanitized);
    }
}
