package foundry.alembic.attribute;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

//
@ApiStatus.ScheduledForRemoval(inVersion = "1.21")
@Deprecated(forRemoval = true)
public interface UUIDFactory {
    UUID getOrCreate(ResourceLocation id);

    static UUID getOrCreate(Level level, ResourceLocation id) {
        if (!level.isClientSide && UUIDSavedData.getOrLoad(level.getServer()).hasKey(id)) {
            return UUIDSavedData.getOrLoad(level.getServer()).getOrCreate(id);
        } else {
            return UUIDManager.INSTANCE.getOrCreate(id);
        }
    }
}
