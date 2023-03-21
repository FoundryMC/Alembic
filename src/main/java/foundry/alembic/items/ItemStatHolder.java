package foundry.alembic.items;

import net.minecraft.world.item.Item;

import java.util.*;

public class ItemStatHolder {
    private static final Map<Item, ItemStat> ITEM_STATS = new HashMap<>();

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

    public static List<UUID> getUUIDs(Item item){
        ItemStat itemStat = ITEM_STATS.get(item);
        List<UUID> uuids = new ArrayList<>();
        if(itemStat == null) return uuids;
        for (ItemStatAttributeData data : itemStat.attributeData()) {
            uuids.add(data.getUUID());
        }
        return uuids;
    }
}


