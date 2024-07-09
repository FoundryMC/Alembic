package foundry.alembic.stats.item;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import foundry.alembic.Alembic;
import foundry.alembic.networking.ClientboundSyncItemStatsPacket;
import foundry.alembic.stats.item.slots.EquipmentSlotType;
import foundry.alembic.util.ConditionalCodecReloadListener;
import foundry.alembic.util.TagOrElements;
import foundry.alembic.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ItemStatManager extends ConditionalCodecReloadListener<ItemStat> {
    private static final ItemStatHolder HOLDER = new ItemStatHolder();
    private static Map<Item, Multimap<EquipmentSlotType, ItemStat>> clientStats;

    public static Collection<ItemStat> getStats(Item item, EquipmentSlotType equipmentSlotType) {
        if (clientStats != null) {
            return clientStats.get(item).get(equipmentSlotType);
        }
        return HOLDER.get(item, equipmentSlotType);
    }

    public static Map<Item, Multimap<EquipmentSlotType, ItemStat>> getStats() {
        if (clientStats != null) {
            return Collections.unmodifiableMap(clientStats);
        }
        return HOLDER.get();
    }

    public static boolean hasStats(Item item) {
        if (clientStats != null) {
            return clientStats.containsKey(item);
        }
        return HOLDER.contains(item);
    }

    // Set map when syncing packet, set null when client logging out
    public static void syncPacket(@Nullable Map<Item, Multimap<EquipmentSlotType, ItemStat>> statMap) {
        clientStats = statMap;
    }

    public static ClientboundSyncItemStatsPacket createPacket() {
        return new ClientboundSyncItemStatsPacket(HOLDER.get());
    }

    public ItemStatManager(ICondition.IContext conditionContext) {
        super(ItemStat.CODEC, conditionContext, Utils.GSON, "alembic/item_stats");
    }

    @Override
    protected void preApply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        HOLDER.clear();
    }

    @Override
    protected void onSuccessfulParse(ItemStat value, ResourceLocation id) {
        HOLDER.add(value);

        if (Alembic.isDebugEnabled()) {
            logger.debug("Adding items stat {} to {}", id, value.items().getTagOrElementLocation());
        }
    }
}
