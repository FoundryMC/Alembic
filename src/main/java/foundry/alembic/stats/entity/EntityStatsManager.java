package foundry.alembic.stats.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import foundry.alembic.util.ConditionalJsonResourceReloadListener;
import foundry.alembic.util.Utils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.Collections;
import java.util.Map;

public class EntityStatsManager extends ConditionalJsonResourceReloadListener {
    private static final BiMap<ResourceLocation, AlembicEntityStats> ID_TO_STATS = HashBiMap.create();
    private static final Map<EntityType<?>, AlembicEntityStats> STATS = new Reference2ObjectOpenHashMap<>();
    private static final Map<EntityType<?>, AlembicEntityStats> STATS_VIEW = Collections.unmodifiableMap(STATS);

    public EntityStatsManager(ICondition.IContext conditionContext) {
        super(conditionContext, Utils.GSON, "alembic/entity_stats");
    }

    public static Map<EntityType<?>, AlembicEntityStats> getView() {
        return STATS_VIEW;
    }

    private static void put(ResourceLocation id, AlembicEntityStats stats) {
        STATS.put(stats.getEntityType(), stats);
        ID_TO_STATS.put(id, stats);
    }

    public static ResourceLocation getId(AlembicEntityStats stats) {
        return ID_TO_STATS.inverse().get(stats);
    }

    static void clear() {
        STATS.clear();
        ID_TO_STATS.clear();
    }

    public static AlembicEntityStats get(EntityType<?> entityType){
        return STATS.get(entityType);
    }

    public static void smartAddResistance(ResourceLocation id, AlembicEntityStats stats) {
        if(!STATS.containsKey(stats.getEntityType()) || get(stats.getEntityType()).getPriority() < stats.getPriority()) {
            put(id, stats);
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : elements.entrySet()) {
            DataResult<AlembicEntityStats> result = AlembicEntityStats.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if (result.error().isPresent()) {
                Alembic.LOGGER.error("Could not read %s. %s".formatted(entry.getKey(), result.error().get().message()));
                continue;
            }
            AlembicEntityStats obj = result.result().get();
            smartAddResistance(entry.getKey(), obj);
        }
        Alembic.LOGGER.debug("Loaded " + STATS.size() + " entity stats");
    }
}
