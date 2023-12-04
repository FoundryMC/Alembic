package foundry.alembic.resources;

import com.google.gson.JsonElement;
import dev.lukebemish.defaultresources.api.ResourceProvider;
import foundry.alembic.Alembic;
import foundry.alembic.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ResourceProviderHelper {
    public static Map<ResourceLocation, JsonElement> readAsJson(String elementPath, Predicate<JsonElement> parsePredicate) {
        Collection<ResourceLocation> typeJsons = ResourceProvider.instance().getResources("alembic_pack", elementPath, id -> id.getPath().endsWith(".json"));
        Map<ResourceLocation, JsonElement> retMap = new LinkedHashMap<>();
        for (ResourceLocation rl : typeJsons) {
            Stream<? extends InputStream> stream = ResourceProvider.instance().getResourceStreams("alembic_pack", rl);
            stream.findFirst().ifPresent(inputStream -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    JsonElement jsonElement = GsonHelper.fromJson(Utils.GSON, reader, JsonElement.class);
                    if (parsePredicate.test(jsonElement)) {
                        retMap.putIfAbsent(rl, jsonElement);
                    } else {
                        Alembic.LOGGER.info("Conditions failed. Skipping {" + rl + "}");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return retMap;
    }

    public static Map<ResourceLocation, JsonElement> readAsJson(String elementPath) {
        return readAsJson(elementPath, jsonElement -> true);
    }
}
