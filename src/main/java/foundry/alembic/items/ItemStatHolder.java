package foundry.alembic.items;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

import java.util.*;

public class ItemStatHolder {
    public static Map<Item, List<Pair<Attribute, AttributeModifier>>> itemStats = new HashMap<>();

    public static void add(Item item, Attribute attribute, AttributeModifier modifier){
        itemStats.put(item, List.of(Pair.of(attribute, modifier)));
    }

    public static void add(Item item, List<Pair<Attribute, AttributeModifier>> attributes){
        itemStats.put(item, attributes);
    }

    public static List<Pair<Attribute, AttributeModifier>> get(Item item){
        return itemStats.get(item);
    }

    public static void clear() {
        itemStats.clear();
    }

    public static boolean contains(Item item){
        return itemStats.containsKey(item);
    }

    public static List<Item> getItems(){
        return (List<Item>) itemStats.keySet();
    }

    public static int size(){
        return itemStats.size();
    }

    public static void remove(Item item){
        itemStats.remove(item);
    }

    public static List<UUID> getUUIDs(Item item){
        List<Pair<Attribute, AttributeModifier>> attributes = itemStats.get(item);
        List<UUID> uuids = new ArrayList<>();
        if(attributes == null) return uuids;
        for (Pair<Attribute, AttributeModifier> attribute : attributes) {
            uuids.add(attribute.getSecond().getId());
        }
        return uuids;
    }
}


