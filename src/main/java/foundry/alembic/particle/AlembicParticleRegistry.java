package foundry.alembic.particle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import foundry.alembic.resources.ResourceProviderHelper;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class AlembicParticleRegistry {
    public static final Map<ResourceLocation, RegistryObject<ParticleType<SimpleParticleType>>> PARTICLES = new HashMap<>();
    public static final Map<String, DeferredRegister<ParticleType<?>>> ID_TO_REGISTER_MAP = new HashMap<>();

    private static DeferredRegister<ParticleType<?>> getRegister(String resourceId) {
        return ID_TO_REGISTER_MAP.computeIfAbsent(resourceId, s -> DeferredRegister.create(ForgeRegistries.Keys.PARTICLE_TYPES, s));
    }

    public static void initAndRegister(IEventBus modBus){
        for (Map.Entry<ResourceLocation, JsonElement> entry : ResourceProviderHelper.readAsJson("particles.json").entrySet()) {
            JsonArray array = entry.getValue().getAsJsonObject().getAsJsonArray("values");
            for (JsonElement particleName : array) {
                String particleStr = particleName.getAsString();
                ResourceLocation rl = particleStr.contains(":") ? new ResourceLocation(particleStr) : new ResourceLocation(entry.getKey().getNamespace(), particleStr);
                RegistryObject<ParticleType<SimpleParticleType>> regObj = getRegister(rl.getNamespace()).register(rl.getPath(), () -> new SimpleParticleType(true));
                PARTICLES.putIfAbsent(rl, regObj);
            }
        }

        for (DeferredRegister<ParticleType<?>> register : ID_TO_REGISTER_MAP.values()) {
            register.register(modBus);
        }
    }
}
