package foundry.alembic.stats.item;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import foundry.alembic.stats.item.slots.EquipmentSlotType;
import foundry.alembic.util.ConditionalJsonResourceReloadListener;
import foundry.alembic.util.TagOrElements;
import foundry.alembic.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.Collection;
import java.util.Map;

public class ItemStatManager extends ConditionalJsonResourceReloadListener {
    private static final ItemStatHolder HOLDER = new ItemStatHolder();

    public static Collection<ItemStat> getStats(Item item, EquipmentSlotType equipmentSlotType) {
        return HOLDER.get(item, equipmentSlotType);
    }

    public static Map<Item, Multimap<EquipmentSlotType, ItemStat>> getStats() {
        return HOLDER.get();
    }

    public static boolean hasStats(Item item) {
        return HOLDER.contains(item);
    }

    public ItemStatManager(ICondition.IContext conditionContext) {
        super(conditionContext, Utils.GSON, "alembic/item_stats");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        HOLDER.clear();
        int numStatsLoaded = 0;
        for(Map.Entry<ResourceLocation, JsonElement> jsonEntry : pObject.entrySet()) {
            DataResult<ItemStat> result = ItemStat.CODEC.parse(JsonOps.INSTANCE, jsonEntry.getValue());
            if (result.error().isPresent()) {
                Alembic.LOGGER.error("Could not read %s. %s".formatted(jsonEntry.getKey(), result.error().get().message()));
                continue;
            }
            ItemStat stat = result.result().get();
            TagOrElements<Item> elements = stat.items();
            Alembic.printInDebug(() -> "Adding items stat %s to %s".formatted(jsonEntry.getKey(), elements.getTagOrElementLocation()));

            HOLDER.add(stat);

            numStatsLoaded++;
        }
        if (Alembic.isDebugEnabled()) {
            Alembic.LOGGER.debug("Loaded " + numStatsLoaded + " items stats");
        }
    }
}