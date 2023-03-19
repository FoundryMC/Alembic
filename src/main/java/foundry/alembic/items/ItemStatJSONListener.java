package foundry.alembic.items;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ItemStatJSONListener extends SimpleJsonResourceReloadListener {
    public static List<ItemStat> itemStats = new ArrayList<>();

    private static final Gson GSON = new Gson();
    public ItemStatJSONListener() {
        super(GSON, "alembic/item_stats");
    }

    public static void register(AddReloadListenerEvent event){
        Alembic.LOGGER.debug("Registering ItemStatJSONListener");
        event.addListener(new ItemStatJSONListener());
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        itemStats.clear();
        ItemStatHolder.clear();
        for(Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()){
            DataResult<ItemStat> result = ItemStat.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if (result.error().isPresent()) {
                Alembic.LOGGER.error("Could not read %s. %s".formatted(entry.getKey(), result.error().get().message()));
                continue;
            }
            ItemStat obj = result.result().get();
            itemStats.add(obj);
            Item item = ForgeRegistries.ITEMS.getValue(obj.id());
            if(item == null) {
                Alembic.LOGGER.error("Could not find item %s".formatted(obj.id()));
                continue;
            }
            Alembic.LOGGER.debug("Adding item stat %s to %s".formatted(obj.id(), item.getDescriptionId()));
            List<Pair<Attribute, AttributeModifier>> modifiers = new ArrayList<>();
            for (ItemStatAttributeData entry1 : obj.attributes()) {
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.tryParse(entry1.getAttribute()));
                if (attribute == null) {
                    Alembic.LOGGER.error("Could not find attribute %s".formatted(entry1.getAttribute()));
                    continue;
                }
                UUID uuid = entry1.getAttribute().equals("alembic:physical_damage") ? ItemUUIDAccess.getbaseAttackDamageUUID() : entry1.getUUIDType();
                AttributeModifier modifier = new AttributeModifier(uuid, "Weapon modifier", entry1.getValue(), entry1.getOperationEnum());
                modifiers.add(Pair.of(attribute, modifier));
            }
            ItemStatHolder.add(item, modifiers);
        }
        Alembic.LOGGER.debug("Loaded " + itemStats.size() + " item stats");
    }

    public static List<ItemStat> getItemStats(){
        return itemStats;
    }

    public static ItemStat getStat(Item item){
        for(ItemStat stat : itemStats){
            if(stat.id().equals(ForgeRegistries.ITEMS.getKey(item))){
                return stat;
            }
        }
        return null;
    }
}
