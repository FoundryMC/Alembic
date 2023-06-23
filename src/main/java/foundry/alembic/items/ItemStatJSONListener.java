package foundry.alembic.items;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import net.minecraft.core.Registry;
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
        ItemStatHolder.ITEM_STATS.clear();
        int numStatsLoaded = 0;
        for(Map.Entry<ResourceLocation, JsonElement> jsonEntry : pObject.entrySet()) {
            DataResult<ItemStat> result = ItemStat.CODEC.parse(JsonOps.INSTANCE, jsonEntry.getValue());
            if (result.error().isPresent()) {
                Alembic.LOGGER.error("Could not read %s. %s".formatted(jsonEntry.getKey(), result.error().get().message()));
                continue;
            }
            ItemStat stat = result.result().get();
            Item item = stat.item();
            ResourceLocation itemId = Registry.ITEM.getKey(item);
            Alembic.ifPrintDebug(() -> {
                Alembic.LOGGER.debug("Adding item stat %s to %s".formatted(jsonEntry.getKey(), itemId));
            });

            ItemStatHolder.ITEM_STATS.put(item, stat);
            numStatsLoaded++;
        }
        Alembic.LOGGER.debug("Loaded " + numStatsLoaded + " item stats");
    }
}
