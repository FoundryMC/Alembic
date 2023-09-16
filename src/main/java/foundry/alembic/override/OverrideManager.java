package foundry.alembic.override;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.Alembic;
import foundry.alembic.damagesource.DamageSourceIdentifier;
import foundry.alembic.util.Utils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public class OverrideManager extends SimpleJsonResourceReloadListener {
    private static final Codec<OverrideStorage> STORAGE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("priority").forGetter(OverrideStorage::priority),
                    Codec.unboundedMap(DamageSourceIdentifier.EITHER_CODEC, AlembicOverride.CODEC).fieldOf("source_overrides").forGetter(OverrideStorage::map)
            ).apply(instance, OverrideStorage::new)
    );

    private static final Map<DamageSourceIdentifier, AlembicOverride> OVERRIDES = new Reference2ObjectOpenHashMap<>();

    public OverrideManager() {
        super(Utils.GSON, "alembic/overrides");
    }

    public static boolean containsKey(DamageSourceIdentifier sourceIdentifier) {
        return OVERRIDES.containsKey(sourceIdentifier);
    }

    public static boolean containsKey(DamageSource damageSource) {
        return containsKey(DamageSourceIdentifier.create(damageSource.msgId));
    }

    public static Map<DamageSourceIdentifier, AlembicOverride> getOverrides() {
        return Collections.unmodifiableMap(OVERRIDES);
    }

    public static AlembicOverride get(DamageSourceIdentifier sourceIdentifier) {
        return OVERRIDES.get(sourceIdentifier);
    }

    private static void put(DamageSourceIdentifier sourceIdentifier, AlembicOverride override) {
        OVERRIDES.put(sourceIdentifier, override);
    }

    public static void smartAddOverride(DamageSourceIdentifier sourceIdentifier, AlembicOverride override) {
        Alembic.printInDebug(() -> "Adding override for " + sourceIdentifier.getSerializedName() + " with override " + override.getId());
        if (containsKey(sourceIdentifier)) {
            if (get(sourceIdentifier).getPriority() < override.getPriority()) {
                Alembic.LOGGER.info("Replacing override for " + sourceIdentifier.getSerializedName() + " with override " + override.getId() + " because it has a higher priority");
                put(sourceIdentifier, override);
            }
        } else {
            put(sourceIdentifier, override);
        }
    }

    static void clearOverrides() {
        OVERRIDES.clear();
    }

    @Nullable
    public static AlembicOverride getOverridesForSource(DamageSource source) {
        return OVERRIDES.get(DamageSourceIdentifier.create(source.msgId));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        clearOverrides();
        for (Map.Entry<ResourceLocation, JsonElement> dataEntry : jsonMap.entrySet()) {
            DataResult<OverrideStorage> dataResult = STORAGE_CODEC.parse(JsonOps.INSTANCE, dataEntry.getValue());
            if (dataResult.error().isPresent()) {
                Alembic.LOGGER.error("Could not read %s. %s".formatted(dataEntry.getKey(), dataResult.error().get().message()));
                continue;
            }

            OverrideStorage storage = dataResult.result().get();
            for (Map.Entry<Either<DamageSourceIdentifier.DefaultWrappedSource, DamageSourceIdentifier>, AlembicOverride> parsedEntry : storage.map.entrySet()) {
                AlembicOverride override = parsedEntry.getValue();

                override.setId(dataEntry.getKey());
                override.setPriority(storage.priority);

                if (parsedEntry.getKey().left().isPresent()) {
                    for (DamageSourceIdentifier id : parsedEntry.getKey().left().get().getIdentifiers()) {
                        smartAddOverride(id, override);
                    }
                } else {
                    smartAddOverride(parsedEntry.getKey().right().get(), override);
                }
            }
        }
        // write the map to a human-readable string
        String logPut = getOverrides().entrySet().stream().map(entry -> entry.getKey().toString() + " -> " + entry.getValue().toString()).reduce((s, s2) -> s + ", " + s2).orElse("Empty");
        Alembic.LOGGER.debug("Loaded overrides: %s".formatted(logPut));
    }

    record OverrideStorage(int priority, Map<Either<DamageSourceIdentifier.DefaultWrappedSource, DamageSourceIdentifier>, AlembicOverride> map) {}
}
