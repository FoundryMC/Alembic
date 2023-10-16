package foundry.alembic.resistances;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import foundry.alembic.util.ConditionalJsonResourceReloadListener;
import foundry.alembic.util.Utils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.*;

public class ResistanceManager extends ConditionalJsonResourceReloadListener {
    private static final Map<EntityType<?>, AlembicResistance> RESISTANCE_MAP = new Reference2ObjectOpenHashMap<>();

    public ResistanceManager(ICondition.IContext conditionContext) {
        super(conditionContext, Utils.GSON, "alembic/resistances");
    }

    public static Collection<AlembicResistance> getValuesView() {
        return Collections.unmodifiableCollection(RESISTANCE_MAP.values());
    }

    private static void put(AlembicResistance resistance) {
        RESISTANCE_MAP.put(resistance.getEntityType(), resistance);
    }


    static void clear() {
        RESISTANCE_MAP.clear();
    }

    public static AlembicResistance get(EntityType<?> entityType){
        return RESISTANCE_MAP.get(entityType);
    }

    public static void smartAddResistance(AlembicResistance resistance) {
        if(!RESISTANCE_MAP.containsKey(resistance.getEntityType()) || get(resistance.getEntityType()).getPriority() < resistance.getPriority()) {
            put(resistance);
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : elements.entrySet()) {
            DataResult<AlembicResistance> result = AlembicResistance.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if (result.error().isPresent()) {
                Alembic.LOGGER.error("Could not read %s. %s".formatted(entry.getKey(), result.error().get().message()));
                continue;
            }
            AlembicResistance obj = result.result().get();
            obj.setId(entry.getKey());
            smartAddResistance(obj);
        }
        Alembic.LOGGER.debug("Loaded " + getValuesView().size() + " entity stats");
    }
}
