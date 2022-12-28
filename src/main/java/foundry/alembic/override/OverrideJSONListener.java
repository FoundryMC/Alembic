package foundry.alembic.override;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import foundry.alembic.Alembic;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OverrideJSONListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    public OverrideJSONListener() {
        super(GSON, "overrides");
    }

    public static void register(AddReloadListenerEvent event){
        Alembic.LOGGER.debug("Registering OverrideJSONListener");
        event.addListener(new OverrideJSONListener());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        AlembicOverrideHolder.clearOverrides();
        pObject.forEach((rl, jsonElement) -> {
            Alembic.LOGGER.debug("Loading override: " + rl);
            JsonObject json = jsonElement.getAsJsonObject();
            String id = json.get("id").getAsString();
            int priority = json.get("priority").getAsInt();
            JsonObject overrides = json.get("sourceOverrides").getAsJsonObject();
            overrides.keySet().forEach(key -> {
                AlembicDamageType damageType = DamageTypeRegistry.getDamageType(Alembic.location(key));
                JsonArray overrideValues = overrides.get(key).getAsJsonArray();
                for (int i = 0; i < overrideValues.size(); i++) {
                    JsonObject ov = overrideValues.get(i).getAsJsonObject();
                    String source = ov.keySet().toArray()[0].toString();
                    float percentage = ov.get(source).getAsFloat();
                    String finalSource = source;
                    if(!Arrays.stream(AlembicOverride.Override.values()).anyMatch(s -> s.toString().equals(finalSource.toUpperCase(Locale.ROOT)))){
                        source = "ENTITY_TYPE";
                        AlembicOverride.Override overrideType = AlembicOverride.Override.valueOf(source.toUpperCase(Locale.ROOT));
                        AlembicOverride override = new AlembicOverride(id, priority, overrideType, percentage);
                        override.setEntityType(ResourceLocation.tryParse(finalSource));
                        AlembicOverrideHolder.smartAddOverride(damageType, override);
                    } else {
                        AlembicOverride.Override overrideType = AlembicOverride.Override.valueOf(source.toUpperCase(Locale.ROOT));
                        AlembicOverride override = new AlembicOverride(id, priority, overrideType, percentage);
                        AlembicOverrideHolder.smartAddOverride(damageType, override);
                    }
                }
            });
        });
    }
}
