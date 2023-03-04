package foundry.alembic.resistances;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import foundry.alembic.Alembic;
import foundry.alembic.override.AlembicOverride;
import foundry.alembic.override.AlembicOverrideHolder;
import foundry.alembic.override.OverrideJSONListener;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ResistanceJsonListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    public ResistanceJsonListener() {
        super(GSON, "mobs");
    }

    public static void register(AddReloadListenerEvent event){
        Alembic.LOGGER.debug("Registering ResistanceJSONListener");
        event.addListener(new ResistanceJsonListener());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        AlembicResistanceHolder.getResistanceMap().clear();
        pObject.forEach((rl, jsonElement) -> {
            Alembic.LOGGER.debug("Loading entity stats: " + rl);
            JsonObject json = jsonElement.getAsJsonObject();
            String id = json.get("id").getAsString();
            Alembic.LOGGER.debug("Loading entity stats: " + id);
            int priority = json.get("priority").getAsInt();
            EntityType<?> entityType = EntityType.byString(json.get("type").getAsString()).orElse(null);
            if(entityType == null){
                Alembic.LOGGER.error("Entity type not found: " + json.get("type").getAsString());
                return;
            }
            JsonObject resistances = json.get("resistances").getAsJsonObject();
            Map<AlembicDamageType, Float> resMap = new HashMap<>();
            Map<AlembicDamageType, Float> damages = new HashMap<>();
            parseDamageTypeObject(resistances, resMap);
            JsonObject damage = json.get("damage").getAsJsonObject();
            parseDamageTypeObject(damage, damages);
            AlembicResistanceHolder.smartAddResistance(new AlembicResistance(entityType, priority, ResourceLocation.tryParse(id), resMap, damages));
        });
        Alembic.LOGGER.debug("Loaded " + AlembicResistanceHolder.getResistanceMap().size() + " entity stats");
    }

    private void parseDamageTypeObject(JsonObject resistances, Map<AlembicDamageType, Float> resMap) {
        resistances.keySet().forEach(key -> {
            AlembicDamageType damageType = DamageTypeRegistry.getDamageType(Alembic.location(key));
            if(damageType == null){
                Alembic.LOGGER.error("Damage type not found: " + key);
                return;
            }
            resMap.put(damageType, resistances.get(key).getAsFloat());
        });
    }
}
