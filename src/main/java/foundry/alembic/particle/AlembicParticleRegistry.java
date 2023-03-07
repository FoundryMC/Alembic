package foundry.alembic.particle;

import foundry.alembic.Alembic;
import foundry.alembic.AlembicConfig;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class AlembicParticleRegistry {
    public static final Map<ResourceLocation, RegistryObject<ParticleType<SimpleParticleType>>> PARTICLES = new HashMap<>();
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Alembic.MODID);

    public static void init(){
        for(String type : AlembicConfig.particles.get()){
            SimpleParticleType particle = new SimpleParticleType(true);
            RegistryObject<ParticleType<SimpleParticleType>> particleTypeRegistryObject = PARTICLE_TYPES.register(type, () -> particle);
            PARTICLES.put(Alembic.location(type), particleTypeRegistryObject);
        }
    }
}
