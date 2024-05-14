package foundry.alembic.override;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.Alembic;
import foundry.alembic.util.ConditionalCodecReloadListener;
import foundry.alembic.util.TagOrElements;
import foundry.alembic.util.Utils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public class OverrideManager extends ConditionalCodecReloadListener<OverrideManager.OverrideStorage> {
    private static Codec<OverrideStorage> createCodec(ICondition.IContext context) {
        return RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("priority").forGetter(OverrideStorage::priority),
                        Codec.unboundedMap(TagOrElements.codec(Registries.DAMAGE_TYPE, context), AlembicOverride.CODEC).fieldOf("source_overrides").forGetter(OverrideStorage::map)
                ).apply(instance, OverrideStorage::new)
        );
    }

    private static final Map<DamageType, AlembicOverride> OVERRIDES = new Reference2ObjectOpenHashMap<>();
    private final RegistryAccess registryAccess;

    public OverrideManager(ICondition.IContext conditionContext, RegistryAccess registryAccess) {
        super(null, conditionContext, Utils.GSON, "alembic/overrides");
        this.registryAccess = registryAccess;
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

    private static void smartAddOverride(DamageType damageType, AlembicOverride override) {
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

    @Nullable
    public static AlembicOverride getOverridesForSource(DamageSource source) {
        return OVERRIDES.get(source.type());
    }

    @Override
    public DynamicOps<JsonElement> makeOps(ResourceManager resourceManager) {
        return RegistryOps.create(JsonOps.INSTANCE, registryAccess);
    }

    @Override
    protected void onSuccessfulParse(OverrideStorage value, ResourceLocation id) {
        for (Map.Entry<TagOrElements.Immediate<DamageType>, AlembicOverride> parsedEntry : value.map.entrySet()) {
            AlembicOverride override = parsedEntry.getValue();

            override.setId(id);
            override.setPriority(value.priority);

            for (Holder<DamageType> type : parsedEntry.getKey().getElements()) {
                smartAddOverride(type.get(), override);
            }
        }
    }

    @Override
    protected void preApply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        OVERRIDES.clear();
        codec = createCodec(context);
    }

    @Override
    protected void postApply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        String logPut = getOverrides().entrySet().stream()
                .map(entry -> entry.getKey() + " -> " + entry.getValue())
                .reduce((s, s2) -> s + ", " + s2)
                .orElse("Empty");
        logger.debug("Loaded overrides: %s".formatted(logPut));
    }

    public record OverrideStorage(int priority, Map<TagOrElements.Immediate<DamageType>, AlembicOverride> map) {}
}
