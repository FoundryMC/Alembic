package foundry.alembic.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class ConditionalCodecReloadListener<T> extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final ICondition.IContext context;
    protected final Gson gson;
    protected final FileToIdConverter converter;
    protected Codec<T> codec;

    public ConditionalCodecReloadListener(Codec<T> codec, ICondition.IContext conditionContext, Gson gson, String directory) {
        this.codec = codec;
        this.context = conditionContext;
        this.gson = gson;
        this.converter = FileToIdConverter.json(directory);
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<ResourceLocation, JsonElement> retMap = new HashMap<>();

        for (Map.Entry<ResourceLocation, Resource> entry : converter.listMatchingResources(pResourceManager).entrySet()) {
            ResourceLocation id = converter.fileToId(entry.getKey());
            Resource resource = entry.getValue();

            try (BufferedReader reader = resource.openAsReader()) {
                JsonElement jsonElement = GsonHelper.fromJson(gson, reader, JsonElement.class);
                if (jsonElement.isJsonNull() || (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().size() == 0)) {
                    logger.info("Skipping loading empty data file {} from {}", id, entry.getKey());
                    continue;
                }

                if (!CraftingHelper.processConditions(jsonElement.getAsJsonObject(), "forge:conditions", context)) {
                    logger.info("Conditions failed. Skipping {" + entry.getKey() + "}");
                    continue;
                }

                retMap.put(id, jsonElement);

            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                logger.error("Couldn't parse data file {} from {}", id, entry.getKey(), e);
            }
        }
        return retMap;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        int numLoaded = 0;
        preApply(pObject, pResourceManager, pProfiler);
        DynamicOps<JsonElement> ops = makeOps(pResourceManager);
        for (Map.Entry<ResourceLocation, JsonElement> jsonEntry : pObject.entrySet()) {
            ResourceLocation id = jsonEntry.getKey();
            if (!shouldParse(id, jsonEntry.getValue())) {
                continue;
            }
            DataResult<T> elementResult = onParse(codec.parse(ops, jsonEntry.getValue()), id);
            if (elementResult.error().isPresent()) {
                logger.error("Could not parse {}. {}", id, elementResult.error().get().message());
                continue;
            }
            T element = elementResult.result().get();
            onSuccessfulParse(element, id);
            numLoaded++;
        }
        if (Alembic.isDebugEnabled()) {
            logger.debug("Loaded {} elements", numLoaded);
        }
        postApply(pObject, pResourceManager, pProfiler);
    }

    protected void preApply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {}

    protected void postApply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {}

    public DynamicOps<JsonElement> makeOps(ResourceManager resourceManager) {
        return JsonOps.INSTANCE;
    }

    protected boolean shouldParse(ResourceLocation id, JsonElement jsonElement) {
        return true;
    }

    protected DataResult<T> onParse(DataResult<T> result, ResourceLocation path) {
        return result;
    }

    protected abstract void onSuccessfulParse(T value, ResourceLocation id);
}
