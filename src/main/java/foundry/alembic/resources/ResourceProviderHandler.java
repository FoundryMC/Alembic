package foundry.alembic.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import foundry.alembic.Alembic;
import io.github.lukebemish.defaultresources.api.ResourceProvider;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ResourceProviderHandler {

    public static Map<ResourceLocation, JsonElement> readAsJson(String elementPath) {
        Collection<ResourceLocation> typeJsons = ResourceProvider.instance().getResources("defaultresources", elementPath, id -> id.getPath().endsWith(".json"));
        Map<ResourceLocation, JsonElement> retMap = new HashMap<>();
        for (ResourceLocation rl : typeJsons) {
            Stream<? extends InputStream> stream = ResourceProvider.instance().getResourceStreams("defaultresources", rl);
            stream.findFirst().ifPresent(inputStream -> {
                JsonObject obj = Alembic.GSON.fromJson(new BufferedReader(new InputStreamReader(inputStream)), JsonObject.class);
                retMap.putIfAbsent(rl, obj);
            });
        }
        return retMap;
    }
}
