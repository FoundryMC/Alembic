package foundry.alembic.attribute;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import foundry.alembic.types.DamageTypeJSONListener;
import foundry.alembic.util.CodecUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class UUIDManager {
    private static final Codec<Map<ResourceLocation, UUID>> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, CodecUtil.STRING_UUID);
    public static final Supplier<Path> UUID_CACHE = Suppliers.memoize(() -> FMLLoader.getGamePath().resolve(".cache/alembic/uuids.json"));
    @Nullable
    private static UUIDManager instance;

    @Nullable
    public static UUIDManager getInstance() {
        return instance;
    }

    private long lastAccess;
    private Thread delayedSaver = null;
    private boolean dirty = false;

    private final Map<ResourceLocation, UUID> uniqueIds;
    private Map<ResourceLocation, UUID> volatileUniqueIds;

    private UUIDManager(Map<ResourceLocation, UUID> uniqueUuids) {
        this.uniqueIds = uniqueUuids;
    }

    public synchronized UUID getOrCreate(ResourceLocation resourceLocation) {
        if (uniqueIds.containsKey(resourceLocation)) {
            return uniqueIds.get(resourceLocation);
        }
        UUID uuid = UUID.randomUUID();
        uniqueIds.put(resourceLocation, uuid);
        startOrUpdate();
        dirty = true;

        return uuid;
    }

    public synchronized UUID getOrCreateVolatile(ResourceLocation resourceLocation) {
        if (volatileUniqueIds == null) {
            volatileUniqueIds = new HashMap<>();
        }
        if (volatileUniqueIds.containsKey(resourceLocation)) {
            return volatileUniqueIds.get(resourceLocation);
        }
        UUID uuid = UUID.randomUUID();
        volatileUniqueIds.put(resourceLocation, uuid);

        return uuid;
    }

    private void startOrUpdate() {
        if (delayedSaver == null || !delayedSaver.isAlive()) {
            delayedSaver = new Thread(() -> {
                while (true) {
                    if (System.nanoTime() - lastAccess >= 1e+10 && instance.dirty) {
                        try {
                            saveCache();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    }
                }
            }, "Alembic Cache Saver");
            delayedSaver.start();
        }

        lastAccess = System.nanoTime();
    }

    public static void loadCache() throws IOException {
        Map<ResourceLocation, UUID> starting;
        if (Files.exists(UUID_CACHE.get())) {
            try (BufferedReader reader = Files.newBufferedReader(UUID_CACHE.get())) {
                Map<ResourceLocation, UUID> immutable = CODEC.parse(JsonOps.INSTANCE, DamageTypeJSONListener.GSON.getAdapter(JsonElement.class).fromJson(reader)).getOrThrow(false, s -> Alembic.LOGGER.error("Could not load UUID cache"));
                starting = new ConcurrentHashMap<>(immutable);
            }
        } else {
            starting = new ConcurrentHashMap<>();
        }
        UUIDManager.instance = new UUIDManager(starting);
    }

    public static void saveCache() throws IOException {
        if (instance.uniqueIds.isEmpty()) {
            return;
        }

        if (Thread.currentThread() == instance.delayedSaver || instance.delayedSaver != null && instance.delayedSaver.isAlive()) {
            instance.delayedSaver.interrupt();
        }

        if (!Files.exists(UUID_CACHE.get())) {
            Files.createDirectories(UUID_CACHE.get().getParent());
            Files.createFile(UUID_CACHE.get());
        }

        JsonElement tag = CODEC.encodeStart(JsonOps.INSTANCE, instance.uniqueIds).getOrThrow(false, s -> Alembic.LOGGER.error("Could not save UUID cache"));
        try (BufferedWriter writer = Files.newBufferedWriter(UUID_CACHE.get())) {
            DamageTypeJSONListener.GSON.getAdapter(JsonElement.class).toJson(writer, tag);
        }

        instance.dirty = false;
    }
}
