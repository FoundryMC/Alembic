package foundry.alembic.override;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.Alembic;
import foundry.alembic.util.ConditionalJsonResourceReloadListener;
import foundry.alembic.util.TagOrElements;
import foundry.alembic.util.Utils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public class OverrideManager extends ConditionalJsonResourceReloadListener {
    private static final Codec<OverrideStorage> STORAGE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("priority").forGetter(OverrideStorage::priority),
                    Codec.unboundedMap(TagOrElements.lazyCodec(Registries.DAMAGE_TYPE), AlembicOverride.CODEC).fieldOf("source_overrides").forGetter(OverrideStorage::map)
            ).apply(instance, OverrideStorage::new)
    );

    private static final Map<DamageType, AlembicOverride> OVERRIDES = new Reference2ObjectOpenHashMap<>();

    public OverrideManager(ICondition.IContext conditionContext) {
        super(conditionContext, Utils.GSON, "alembic/overrides");
    }

    public static boolean containsKey(DamageType damageType) {
        return OVERRIDES.containsKey(damageType);
    }

    public static boolean containsKey(DamageSource damageSource) {
        return containsKey(damageSource.type());
    }

    public static Map<DamageType, AlembicOverride> getOverrides() {
        return Collections.unmodifiableMap(OVERRIDES);
    }

    public static AlembicOverride get(DamageType damageType) {
        return OVERRIDES.get(damageType);
    }

    private static void put(DamageType damageType, AlembicOverride override) {
        OVERRIDES.put(damageType, override);
    }

    public static void smartAddOverride(DamageType damageType, AlembicOverride override) {
        Alembic.printInDebug(() -> "Adding override for " + damageType.msgId() + " with override " + override.getId());
        if (containsKey(damageType)) {
            if (get(damageType).getPriority() < override.getPriority()) {
                Alembic.LOGGER.info("Replacing override for " + damageType.msgId() + " with override " + override.getId() + " because it has a higher priority");
                put(damageType, override);
            }
        } else {
            put(damageType, override);
        }
    }

    static void clearOverrides() {
        OVERRIDES.clear();
    }

    @Nullable
    public static AlembicOverride getOverridesForSource(DamageSource source) {
        return OVERRIDES.get(source.type());
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
            for (Map.Entry<TagOrElements.Lazy<DamageType>, AlembicOverride> parsedEntry : storage.map.entrySet()) {
                AlembicOverride override = parsedEntry.getValue();

                override.setId(dataEntry.getKey());
                override.setPriority(storage.priority);

                RegistryAccess access = ServerLifecycleHooks.getCurrentServer().registryAccess();

                for (DamageType type : parsedEntry.getKey().getElements(access)) {
                    smartAddOverride(type, override);
                }
            }
        }
        // write the map to a human-readable string
        String logPut = getOverrides().entrySet().stream().map(entry -> entry.getKey().toString() + " -> " + entry.getValue().toString()).reduce((s, s2) -> s + ", " + s2).orElse("Empty");
        Alembic.LOGGER.debug("Loaded overrides: %s".formatted(logPut));
    }

    record OverrideStorage(int priority, Map<TagOrElements.Lazy<DamageType>, AlembicOverride> map) {}
}
