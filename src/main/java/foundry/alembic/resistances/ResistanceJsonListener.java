package foundry.alembic.resistances;

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

import java.util.*;

public class ResistanceJsonListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    public ResistanceJsonListener() {
        super(GSON, "alembic/resistances");
    }

    public static void register(AddReloadListenerEvent event){
        Alembic.printInDebug(() -> "Registering ResistanceJSONListener");
        event.addListener(new ResistanceJsonListener());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        AlembicResistanceHolder.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : elements.entrySet()) {
            DataResult<AlembicResistance> result = AlembicResistance.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if (result.error().isPresent()) {
                Alembic.LOGGER.error("Could not read %s. %s".formatted(entry.getKey(), result.error().get().message()));
                continue;
            }
            AlembicResistance obj = result.result().get();
            obj.setId(entry.getKey());
            AlembicResistanceHolder.smartAddResistance(obj);
        }
        Alembic.LOGGER.debug("Loaded " + AlembicResistanceHolder.getValuesView().size() + " entity stats");
    }
}
