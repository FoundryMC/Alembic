package foundry.alembic.types.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class AlembicParticleTag implements AlembicTag {
    public static final Codec<AlembicParticleTag> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                ParticleTypes.CODEC.fieldOf("particle_options").forGetter(alembicParticleTag -> alembicParticleTag.particleOptions),
                Codec.BOOL.optionalFieldOf("player_only", false).forGetter(alembicParticleTag -> alembicParticleTag.playerOnly)
        ).apply(instance, AlembicParticleTag::new)
    );
    private final ParticleOptions particleOptions;
    private final boolean playerOnly;
    public AlembicParticleTag(ParticleOptions particleOptions, boolean playerOnly){
        this.particleOptions = particleOptions;
        this.playerOnly = playerOnly;
    }


    @Override
    public void run(ComposedData data) {
        LivingEntity entity = data.get(ComposedDataType.TARGET_ENTITY);
        float damage = data.get(ComposedDataType.FINAL_DAMAGE);
        Level level = data.get(ComposedDataType.LEVEL);
        DamageSource source = data.get(ComposedDataType.ORIGINAL_SOURCE);
        float particleCount = damage < 1 ? 1 : damage/2f;
        if(playerOnly){
            if(source.getDirectEntity() != null){
                if(source.getDirectEntity() instanceof Player){
                    ((ServerLevel) level).sendParticles(particleOptions, entity.getX(), entity.getY() + entity.getBbHeight()/2f, entity.getZ(),
                            (int) Math.ceil(particleCount * 2),
                            0,0,0,
                            0.35f);
                }
            }
        } else {
            ((ServerLevel) level).sendParticles(particleOptions, entity.getX(), entity.getY() + entity.getBbHeight()/2f, entity.getZ(),
                    (int) Math.ceil(particleCount * 2),
                    0,0,0,
                    0.35f);
        }
    }

    @Override
    public AlembicTagType<?> getType() {
        return AlembicTagType.PARTICLE;
    }

    @Override
    public String toString() {
        return "AlembicParticleTag, Particle options: " + particleOptions.getType().getClass().getSimpleName();
    }
}
