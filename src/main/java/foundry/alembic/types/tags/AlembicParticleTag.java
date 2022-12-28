package foundry.alembic.types.tags;

import com.mojang.math.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class AlembicParticleTag implements AlembicTag<Level, Entity, Integer> {
    public ResourceLocation particleType;
    public Vector3f color;
    public float alpha;
    public AlembicParticleTag(ResourceLocation particleType, Vector3f color, float alpha){
        this.particleType = particleType;
        this.color = color;
        this.alpha = alpha;
    }


    @Override
    public void run(Level level, Entity entity, Integer damage) {

    }

    @Override
    public void run(Level level, LivingEntity entity, float damage) {
        if(particleType.getPath().equals("dust")){
            ((ServerLevel) level).sendParticles(new DustParticleOptions(color, alpha), entity.getX(), entity.getY() + entity.getBbHeight()/2f, entity.getZ(),
                    25,
                    level.random.nextFloat()-0.5f,
                    level.random.nextFloat()-0.5f,
                    level.random.nextFloat()-0.5f,
                    0.15f);
            return;
        }
        SimpleParticleType type = (SimpleParticleType)ForgeRegistries.PARTICLE_TYPES.getValue(particleType);
        if(type == null) return;
        ((ServerLevel) level).sendParticles(type, entity.getX(), entity.getY() + entity.getBbHeight()/2f, entity.getZ(),
                (int) Math.ceil(damage),
                level.random.nextFloat()-0.5f,
                level.random.nextFloat()-0.5f,
                level.random.nextFloat()-0.5f,
                0.15f);
    }

    @Override
    public String toString() {
        return "AlembicParticleTag";
    }
}
