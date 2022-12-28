package foundry.alembic.types;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.math.Vector3f;
import foundry.alembic.Alembic;
import foundry.alembic.types.tags.AlembicTag;
import foundry.alembic.types.tags.AlembicTagDataHolder;
import foundry.alembic.types.tags.AlembicTagRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class DamageTypeJSONListener extends SimpleJsonResourceReloadListener {
    public static final Gson GSON = new Gson();
    public DamageTypeJSONListener() {
        super(GSON, "damage_types");
    }

    public static void register(AddReloadListenerEvent event){
        Alembic.LOGGER.debug("Registering DamageTypeJSONListener");
        event.addListener(new DamageTypeJSONListener());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager rm, ProfilerFiller profiler) {
        List<ResourceLocation> passed = new ArrayList<>();
        object.forEach((rl, jsonElement) -> {
            JsonObject json = jsonElement.getAsJsonObject();
            String id = json.get("id").getAsString();
            ResourceLocation damageType = Alembic.location(id);
            int priority = json.get("priority").getAsInt();
            double base = json.get("default").getAsDouble();
            double min = json.get("min").getAsDouble();
            double max = json.get("max").getAsDouble();
            boolean hasShielding = json.get("shielding").getAsBoolean();
            boolean hasResistance = json.get("resistance").getAsBoolean();
            boolean hasAbsorption = json.get("absorption").getAsBoolean();
            boolean enableParticles = json.get("particles").getAsBoolean();
            List<AlembicTag<?,?,?>> tags = new ArrayList<>();
            json.get("tags").getAsJsonArray().forEach(jsonElement1 -> {
                JsonObject tag = jsonElement1.getAsJsonObject();
                String tagId = tag.get("id").getAsString();
                JsonArray tagValues = tag.get("properties").getAsJsonArray();
                for (int i = 0; i < tagValues.size(); i++) {
                    //Alembic.LOGGER.error(tagValues.get(i).getAsString());
                    if(tagValues.get(i).isJsonObject()){
                        JsonObject particleJson = tagValues.get(i).getAsJsonObject();
                        AtomicReference<Vector3f> color = new AtomicReference<>(new Vector3f(1, 1, 1));
                        AtomicReference<Float> alpha = new AtomicReference<>((float) 1);
                        AtomicReference<String> particleId = new AtomicReference<>("");
                        particleJson.keySet().forEach(key -> {
                            particleId.set(key);
                            JsonArray particleValues = particleJson.get(key).getAsJsonArray();
                            color.set(new Vector3f(particleValues.get(0).getAsFloat(), particleValues.get(1).getAsFloat(), particleValues.get(2).getAsFloat()));
                            alpha.set(particleValues.get(3).getAsFloat());
                        });
                        AlembicTagDataHolder dataHolder = new AlembicTagDataHolder(particleId.get(), color.get(), alpha.get());
                        if(AlembicTagRegistry.isRegistered(tagId)){
                            Alembic.LOGGER.debug("Registering tag {} for damage type {}", tagId, damageType);
                            tags.add(AlembicTagRegistry.create(tagId, dataHolder));
                        } else {
                            Alembic.LOGGER.error("Tag {} is not registered! Valid tags are {}", tagId, AlembicTagRegistry.getRegisteredTags());
                        }
                    }
                }
            });
            int color = Integer.parseInt(json.get("color").getAsString().replace("#",""), 16);
            if(DamageTypeRegistry.doesDamageTypeExist(damageType)){
                if(priority < DamageTypeRegistry.getDamageType(damageType).getPriority()){
                    Alembic.LOGGER.debug("Damage type " + damageType + " already exists with a higher priority. Skipping.");
                } else {
                    Alembic.LOGGER.debug("Damage type " + damageType + " already exists with a lower priority. Overwriting.");
                    AlembicDamageType type = DamageTypeRegistry.getDamageType(damageType);
                    type.setPriority(priority);
                    AlembicAttribute attribute = type.getAttribute();
                    attribute.setBaseValue(base);
                    type.setBase(base);
                    attribute.setMinValue(min);
                    type.setMin(min);
                    attribute.setMaxValue(max);
                    type.setMax(max);
                    attribute.setDescriptionId("attribute.name." + id);
                    type.setHasShielding(hasShielding);
                    type.setHasResistance(hasResistance);
                    type.setHasAbsorption(hasAbsorption);
                    type.setEnableParticles(enableParticles);
                    type.setColor(color);
                    type.clearTags();
                    tags.forEach(type::addTag);
                    passed.add(damageType);
                }
            }
        });
    }
}
