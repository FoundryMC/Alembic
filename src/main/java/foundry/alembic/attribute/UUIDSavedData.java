package foundry.alembic.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import foundry.alembic.Alembic;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApiStatus.ScheduledForRemoval(inVersion = "1.21")
@Deprecated(forRemoval = true, since = "1.0.")
public class UUIDSavedData extends SavedData implements UUIDFactory {
    public static final UnboundedMapCodec<ResourceLocation, UUID> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, UUIDUtil.CODEC);

    public static final String ATTR_MODIFIER_ID = "alembic_attr_mod_uuids";

    private final Map<ResourceLocation, UUID> uniqueIds;
    private final RandomSource randomSource;

    public UUIDSavedData() {
        this(new HashMap<>());
    }

    private UUIDSavedData(Map<ResourceLocation, UUID> uniqueIds) {
        this.uniqueIds = uniqueIds;
        randomSource = RandomSource.create();
    }

    @Override
    public UUID getOrCreate(ResourceLocation resourceLocation) {
        UUID uuid = uniqueIds.computeIfAbsent(resourceLocation, resourceLocation1 -> UUID.nameUUIDFromBytes(resourceLocation1.toString().getBytes()));
        setDirty();

        return uuid;
    }

    boolean hasKey(ResourceLocation id) {
        return uniqueIds.containsKey(id);
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, uniqueIds).getOrThrow(false, Alembic.LOGGER::error);
    }

    public static UUIDSavedData load(CompoundTag compoundTag) {
        return new UUIDSavedData(new HashMap<>(CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound(ATTR_MODIFIER_ID)).getOrThrow(false, Alembic.LOGGER::error)));
    }

    public static UUIDSavedData getOrLoad(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(UUIDSavedData::load, UUIDSavedData::new, ATTR_MODIFIER_ID);
    }
}
