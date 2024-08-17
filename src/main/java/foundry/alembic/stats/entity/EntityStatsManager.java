package foundry.alembic.stats.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import foundry.alembic.util.ConditionalCodecReloadListener;
import foundry.alembic.util.Utils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.Collections;
import java.util.Map;

public class EntityStatsManager extends ConditionalCodecReloadListener<AlembicEntityStats> {
    private static final BiMap<ResourceLocation, AlembicEntityStats> ID_TO_STATS = HashBiMap.create();
    private static final Map<EntityType<?>, AlembicEntityStats> STATS = new Reference2ObjectOpenHashMap<>();
    private static final Map<EntityType<?>, AlembicEntityStats> STATS_VIEW = Collections.unmodifiableMap(STATS);

    private final RegistryAccess registryAccess;

    public EntityStatsManager(ICondition.IContext conditionContext, RegistryAccess registryAccess) {
        super(AlembicEntityStats.codec(conditionContext), conditionContext, Utils.GSON, "alembic/entity_stats");
        this.registryAccess = registryAccess;
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

    @Override
    public DynamicOps<JsonElement> makeOps(ResourceManager resourceManager) {
        return RegistryOps.create(JsonOps.INSTANCE, registryAccess);
    }

    @Override
    protected void preApply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        clear();
    }

    @Override
    protected void onSuccessfulParse(AlembicEntityStats value, ResourceLocation id) {
        if(!STATS.containsKey(value.getEntityType()) || get(value.getEntityType()).getPriority() < value.getPriority()) {
            put(id, value);
        }
    }
}
