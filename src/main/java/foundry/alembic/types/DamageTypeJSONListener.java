package foundry.alembic.types;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.Map;

public class DamageTypeJSONListener extends SimpleJsonResourceReloadListener {
    public static final Gson GSON = new Gson();
    public DamageTypeJSONListener() {
        super(GSON, "alembic/damage_types");
    }

    public static void register(AddReloadListenerEvent event){
        Alembic.LOGGER.debug("Registering DamageTypeJSONListener");
        event.addListener(new DamageTypeJSONListener());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager rm, ProfilerFiller profiler) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : elements.entrySet()) {
            DataResult<AlembicDamageType> result =  AlembicDamageType.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if (result.error().isPresent()) {
                Alembic.LOGGER.error("Could not read %s. %s".formatted(entry.getKey(), result.error().get().message()));
                continue;
            }
            AlembicDamageType type = result.getOrThrow(false, Alembic.LOGGER::error);
            type.handlePostParse(entry.getKey());


            if (DamageTypeRegistry.doesDamageTypeExist(type.getId())) {
                if (type.getPriority() < DamageTypeRegistry.getDamageType(type.getId()).getPriority()) {
                    Alembic.LOGGER.debug("Damage type %s already exists with a higher priority. Skipping.".formatted(type.getId()));
                } else {
                    Alembic.LOGGER.debug("Damage type %s already exists with a lower priority. Overwriting.".formatted(type.getId()));
                    DamageTypeRegistry.replaceWithData(type);
                }
            }
        }
    }
}
