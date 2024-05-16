package foundry.alembic.stats.shield;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import foundry.alembic.networking.ClientboundSyncShieldStatsPacket;
import foundry.alembic.util.ConditionalCodecReloadListener;
import foundry.alembic.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShieldStatManager extends ConditionalCodecReloadListener<ShieldBlockStat> {
    private static final List<ShieldBlockStat> HOLDER = new ArrayList<>();
    private static List<ShieldBlockStat> clientStats;

    public ShieldStatManager(ICondition.IContext conditionContext) {
        super(ShieldBlockStat.CODEC, conditionContext, Utils.GSON, "alembic/shield_stats");
    }

//    private static List<ShieldBlockStat> getTrueStats() {
//        return clientStats != null ? clientStats : HOLDER;
//    }

    public static List<ShieldBlockStat> getStats() {
        return Collections.unmodifiableList(clientStats != null ? clientStats : HOLDER);
    }

    public static Collection<ShieldBlockStat> getStats(Item item) {
        List<ShieldBlockStat> stats = new ArrayList<>();
        for (ShieldBlockStat stat : (clientStats != null ? clientStats : HOLDER)) {
            if(stat.item().is(item)){
                stats.add(stat);
            }
        }
        return stats;
    }

    public static void syncPacket(@Nullable List<ShieldBlockStat> shieldStats) {
        clientStats = shieldStats;
    }

    public static ClientboundSyncShieldStatsPacket createPacket() {
        return new ClientboundSyncShieldStatsPacket(HOLDER);
    }

    @Override
    protected void preApply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        HOLDER.clear();
    }

    @Override
    protected DataResult<ShieldBlockStat> onParse(DataResult<ShieldBlockStat> result, ResourceLocation path) {
        return result.flatMap(shieldBlockStat -> {
            if (!shieldBlockStat.item().canPerformAction(ToolActions.SHIELD_BLOCK)) {
                return DataResult.error(() -> "Item cannot block");
            }
            return DataResult.success(shieldBlockStat);
        });
    }

    @Override
    protected void onSuccessfulParse(ShieldBlockStat value, ResourceLocation path) {
        HOLDER.add(value);
    }
}
