package foundry.alembic.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import foundry.alembic.Alembic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class ConditionalJsonResourceReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {

    protected final ICondition.IContext context;
    protected final Gson gson;
    protected final String directory;

    public ConditionalJsonResourceReloadListener(ICondition.IContext conditionContext, Gson gson, String directory) {
        this.context = conditionContext;
        this.gson = gson;
        this.directory = directory;
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<ResourceLocation, JsonElement> retMap = new HashMap<>();

        // TODO: 1.20 replace with FileToIdConverter
        for (Map.Entry<ResourceLocation, Resource> entry : pResourceManager.listResources(directory, resourceLocation -> resourceLocation.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation id = Utils.sanitize(entry.getKey(), directory + "/", ".json");
            Resource resource = entry.getValue();

            try (BufferedReader reader = resource.openAsReader()) {
                JsonElement jsonElement = GsonHelper.fromJson(gson, reader, JsonElement.class);
                if (jsonElement == null) {
                    Alembic.LOGGER.error("Couldn't load data file {} from {} as it's null or empty", id, entry.getKey());
                    continue;
                }

                if (!CraftingHelper.processConditions(jsonElement.getAsJsonObject(), "forge:conditions", context)) {
                    Alembic.LOGGER.info("Conditions failed. Skipping {" + entry.getKey() + "}");
                    continue;
                }

                retMap.put(id, jsonElement);

            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                Alembic.LOGGER.error("Couldn't parse data file {} from {}", id, entry.getKey(), e);
            }
        }
        return retMap;
    }
}
