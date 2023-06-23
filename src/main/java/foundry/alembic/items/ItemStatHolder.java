package foundry.alembic.items;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.stream.Collectors;

public class ItemStatHolder {
    private static final Map<Item, ItemStat> ITEM_STATS = new Reference2ObjectOpenHashMap<>();

    public static ItemStat get(Item item){
        return ITEM_STATS.get(item);
    }

    static void clear() {
        ITEM_STATS.clear();
    }

    static void put(Item item, ItemStat stat) {
        ITEM_STATS.put(item, stat);
    }

    public static boolean contains(Item item){
        return ITEM_STATS.containsKey(item);
    }

    public static Collection<Item> getItems(){
        return Collections.unmodifiableCollection(ITEM_STATS.keySet());
    }

    public static Set<UUID> getUUIDs(Item item) {
        if (!ITEM_STATS.containsKey(item)) {
            return Set.of();
        }
        return ITEM_STATS.get(item).attributeData().stream().map(ItemStatAttributeData::getUUID).collect(Collectors.toSet());
    }
}


