package foundry.alembic.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import foundry.alembic.Alembic;
import net.minecraft.SharedConstants;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UUIDSavedData extends SavedData {
    public static final UnboundedMapCodec<ResourceLocation, UUID> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, UUIDUtil.CODEC);

    @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
    @Deprecated(forRemoval = true)
    public static final String OLD_ATTR_MOD_ID = "attr_mod_uuids";
    public static final String ATTR_MODIFIER_ID = "alembic_attr_mod_uuids";

    private final Map<ResourceLocation, UUID> uniqueIds;

    public UUIDSavedData() {
        uniqueIds = new HashMap<>();
    }

    private UUIDSavedData(CompoundTag savedDataTag) {
        uniqueIds = new HashMap<>(CODEC.parse(NbtOps.INSTANCE, savedDataTag).getOrThrow(false, Alembic.LOGGER::error));
    }

    public UUID getOrCreate(ResourceLocation resourceLocation) {
        if (uniqueIds.containsKey(resourceLocation)) {
            return uniqueIds.get(resourceLocation);
        }
        UUID uuid = UUID.randomUUID();
        uniqueIds.put(resourceLocation, uuid);
        setDirty();

        return uuid;
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, uniqueIds).getOrThrow(false, Alembic.LOGGER::error);
    }

    public static UUIDSavedData load(CompoundTag compoundTag) {
        return new UUIDSavedData(compoundTag.getCompound(ATTR_MODIFIER_ID));
    }

    public static UUIDSavedData getOrLoad(MinecraftServer server) {
        UUIDSavedData oldFormat = server.overworld().getDataStorage().get(UUIDSavedData::load, OLD_ATTR_MOD_ID);
        if (oldFormat != null) {
            return oldFormat;
        }
        return server.overworld().getDataStorage().computeIfAbsent(UUIDSavedData::load, UUIDSavedData::new, ATTR_MODIFIER_ID);
    }
}
