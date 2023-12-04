package foundry.alembic.stats.shield;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import foundry.alembic.util.ConditionalJsonResourceReloadListener;
import foundry.alembic.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ShieldStatManager extends ConditionalJsonResourceReloadListener {
    private static final List<ShieldBlockStat> HOLDER = new ArrayList<>();

    public ShieldStatManager(ICondition.IContext conditionContext) {
        super(conditionContext, Utils.GSON, "alembic/shield_stats");
    }

    public static Collection<ShieldBlockStat> getStats(Item item){
        List<ShieldBlockStat> stats = new ArrayList<>();
        for(ShieldBlockStat stat : HOLDER){
            if(stat.item().is(item)){
                stats.add(stat);
            }
        }
        return stats;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        HOLDER.clear();
        int numStatsLoaded = 0;
        for(Map.Entry<ResourceLocation, JsonElement> jsonEntry : pObject.entrySet()) {
            DataResult<ShieldBlockStat> stat = ShieldBlockStat.CODEC.parse(JsonOps.INSTANCE, jsonEntry.getValue());
            if (stat.error().isPresent()) {
                Alembic.LOGGER.error("Could not read %s. %s".formatted(jsonEntry.getKey(), stat.error().get().message()));
                continue;
            }
            Alembic.printInDebug(() -> "Adding shield stat %s".formatted(jsonEntry.getKey()));
            ShieldBlockStat blockStat = stat.result().get();
            if(!blockStat.item().canPerformAction(ToolActions.SHIELD_BLOCK)){
                Alembic.LOGGER.error("Could not read %s. %s".formatted(jsonEntry.getKey(), "Item cannot block"));
                continue;
            }
            HOLDER.add(blockStat);
            numStatsLoaded++;
        }
        if (Alembic.isDebugEnabled()) {
            Alembic.LOGGER.debug("Loaded " + numStatsLoaded + " shield stats");
        }
    }
}
