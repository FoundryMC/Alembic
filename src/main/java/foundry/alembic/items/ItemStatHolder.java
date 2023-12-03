package foundry.alembic.items;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import foundry.alembic.items.slots.EquipmentSlotType;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.item.Item;

import java.util.*;

public class ItemStatHolder {
    private final Map<Item, Multimap<EquipmentSlotType, ItemStat>> stats = new Reference2ObjectOpenHashMap<>();

    public Collection<ItemStat> get(Item item, EquipmentSlotType slot) {
        if (!stats.containsKey(item)) {
            return Collections.emptyList();
        }
        Multimap<EquipmentSlotType, ItemStat> map = stats.get(item);
        return Collections.unmodifiableCollection(map.get(slot));
    }

    public void add(ItemStat stat) {
        stat.items().getElements().forEach(item -> {
            Multimap<EquipmentSlotType, ItemStat> multimap = stats.computeIfAbsent(item, item1 -> HashMultimap.create());
            stat.equipmentSlots().forEach(equipmentSlotType -> multimap.put(equipmentSlotType, stat));
        });
    }

    public Map<Item, Multimap<EquipmentSlotType, ItemStat>> get() {
        return Collections.unmodifiableMap(stats);
    }

    public void clear() {
        stats.clear();
    }

    public boolean contains(Item item) {
        return stats.containsKey(item);
    }
}


