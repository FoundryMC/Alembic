package foundry.alembic.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import foundry.alembic.Alembic;
import io.github.lukebemish.defaultresources.api.ResourceProvider;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ResourceProviderHandler {

    public static Map<ResourceLocation, JsonElement> readAsJson(String elementPath) {
        Collection<ResourceLocation> typeJsons = ResourceProvider.instance().getResources("alembic_pack", elementPath, id -> id.getPath().endsWith(".json"));
        Map<ResourceLocation, JsonElement> retMap = new LinkedHashMap<>();
        for (ResourceLocation rl : typeJsons) {
            Stream<? extends InputStream> stream = ResourceProvider.instance().getResourceStreams("alembic_pack", rl);
            stream.findFirst().ifPresent(inputStream -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    JsonObject obj = Alembic.GSON.getAdapter(JsonObject.class).fromJson(reader);
                    retMap.putIfAbsent(rl, obj);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return retMap;
    }
}
