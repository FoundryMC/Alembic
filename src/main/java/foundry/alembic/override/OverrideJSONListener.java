package foundry.alembic.override;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.Alembic;
import foundry.alembic.damagesource.AlembicDamageSourceIdentifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.Map;

public class OverrideJSONListener extends SimpleJsonResourceReloadListener {
    private static final Codec<OverrideStorage> STORAGE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("priority").forGetter(OverrideStorage::priority),
                    Codec.unboundedMap(AlembicDamageSourceIdentifier.EITHER_CODEC, AlembicOverride.CODEC).fieldOf("source_overrides").forGetter(OverrideStorage::map)
            ).apply(instance, OverrideStorage::new)
    );

    private static final Gson GSON = new Gson();
    public OverrideJSONListener() {
        super(GSON, "alembic/overrides");
    }

    public static void register(AddReloadListenerEvent event){
        Alembic.LOGGER.debug("Registering OverrideJSONListener");
        event.addListener(new OverrideJSONListener());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        AlembicOverrideHolder.clearOverrides();
        for (Map.Entry<ResourceLocation, JsonElement> dataEntry : pObject.entrySet()) {
            DataResult<OverrideStorage> dataResult = STORAGE_CODEC.parse(JsonOps.INSTANCE, dataEntry.getValue());
            if (dataResult.error().isPresent()) {
                Alembic.LOGGER.error("Could not read %s. %s".formatted(dataEntry.getKey(), dataResult.error().get().message()));
                continue;
            }

            OverrideStorage storage = dataResult.result().get();
            for (Map.Entry<Either<AlembicDamageSourceIdentifier.DefaultWrappedSources, AlembicDamageSourceIdentifier>, AlembicOverride> parsedEntry : storage.map.entrySet()) {
                AlembicOverride override = parsedEntry.getValue();

                override.setId(dataEntry.getKey());
                override.setPriority(storage.priority);

                if (parsedEntry.getKey().left().isPresent()) {
                    for (AlembicDamageSourceIdentifier id : parsedEntry.getKey().left().get().getIdentifiers()) {
                        AlembicOverrideHolder.smartAddOverride(id, override, dataEntry.getKey());
                    }
                } else {
                    AlembicOverrideHolder.smartAddOverride(parsedEntry.getKey().right().get(), override, dataEntry.getKey());
                }
            }
        }
        // write the map to a human readable string
        String logPut = AlembicOverrideHolder.getOverrides().entrySet().stream().map(entry -> entry.getKey().toString() + " -> " + entry.getValue().toString()).reduce((s, s2) -> s + ", " + s2).orElse("Empty");
        Alembic.LOGGER.debug("Loaded overrides: %s".formatted(logPut));
    }

    record OverrideStorage(int priority, Map<Either<AlembicDamageSourceIdentifier.DefaultWrappedSources, AlembicDamageSourceIdentifier>, AlembicOverride> map) {}
}
