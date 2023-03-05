package foundry.alembic.types.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class AlembicParticleTag implements AlembicTag {
    public static final Codec<AlembicParticleTag> CODEC = ParticleTypes.CODEC.fieldOf("particle_options").xmap(AlembicParticleTag::new, alembicParticleTag -> alembicParticleTag.particleOptions).codec();
    private final ParticleOptions particleOptions;
    public AlembicParticleTag(ParticleOptions particleOptions) {
        this.particleOptions = particleOptions;
    }


    @Override
    public void run(ComposedData data) {

    }

    @Override
    public void run(Level level, LivingEntity entity, float damage, DamageSource originalSource) {
        ((ServerLevel) level).sendParticles(particleOptions, entity.getX(), entity.getY() + entity.getBbHeight()/2f, entity.getZ(),
                (int) Math.ceil(damage),
                level.random.nextFloat()-0.5f,
                level.random.nextFloat()-0.5f,
                level.random.nextFloat()-0.5f,
                0.15f);
    }

    @Override
    public AlembicTagType<?> getType() {
        return AlembicTagType.PARTICLE;
    }

    @Override
    public String toString() {
        return "AlembicParticleTag";
    }
}
