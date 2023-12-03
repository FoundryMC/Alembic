package foundry.alembic.stats;

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

import java.util.*;

public class StatsManager extends ConditionalJsonResourceReloadListener {
    private static final Map<EntityType<?>, AlembicEntityStats> RESISTANCE_MAP = new Reference2ObjectOpenHashMap<>();

    public StatsManager(ICondition.IContext conditionContext) {
        super(conditionContext, Utils.GSON, "alembic/stats");
    }

    public static Collection<AlembicEntityStats> getValuesView() {
        return Collections.unmodifiableCollection(RESISTANCE_MAP.values());
    }

    private static void put(AlembicEntityStats resistance) {
        RESISTANCE_MAP.put(resistance.getEntityType(), resistance);
    }


    static void clear() {
        RESISTANCE_MAP.clear();
    }

    public static AlembicEntityStats get(EntityType<?> entityType){
        return RESISTANCE_MAP.get(entityType);
    }

    public static void smartAddResistance(AlembicEntityStats resistance) {
        if(!RESISTANCE_MAP.containsKey(resistance.getEntityType()) || get(resistance.getEntityType()).getPriority() < resistance.getPriority()) {
            put(resistance);
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
            obj.setId(entry.getKey());
            smartAddResistance(obj);
        }
        Alembic.LOGGER.debug("Loaded " + getValuesView().size() + " entity stats");
    }
}
