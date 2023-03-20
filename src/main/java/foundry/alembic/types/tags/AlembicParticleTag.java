package foundry.alembic.types.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class AlembicParticleTag implements AlembicTag {
    public static final Codec<AlembicParticleTag> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                ParticleTypes.CODEC.fieldOf("particle_options").forGetter(alembicParticleTag -> alembicParticleTag.particleOptions),
                Codec.BOOL.optionalFieldOf("player_only", false).forGetter(alembicParticleTag -> alembicParticleTag.playerOnly),
                ParticleTagEntityFilter.CODEC.listOf().optionalFieldOf("filters").forGetter(alembicParticleTag -> alembicParticleTag.entityFilter),
                Codec.FLOAT.optionalFieldOf("particle_speed").forGetter(alembicParticleTag -> alembicParticleTag.particleSpeed)
        ).apply(instance, AlembicParticleTag::new)
    );
    private final ParticleOptions particleOptions;
    private final boolean playerOnly;
    private final Optional<List<ParticleTagEntityFilter>> entityFilter;
    private final Optional<Float> particleSpeed;
    public AlembicParticleTag(ParticleOptions particleOptions, boolean playerOnly, Optional<List<ParticleTagEntityFilter>> entityFilter, Optional<Float> particleSpeed) {
        this.particleOptions = particleOptions;
        this.playerOnly = playerOnly;
        this.entityFilter = entityFilter;
        this.particleSpeed = particleSpeed;
    }

    public AlembicParticleTag(ParticleOptions particleOptions, boolean playerOnly) {
        this(particleOptions, playerOnly, Optional.empty(), Optional.empty());
    }


    @Override
    public void run(ComposedData data) {
        LivingEntity entity = data.get(ComposedDataType.TARGET_ENTITY);
        float damage = data.get(ComposedDataType.FINAL_DAMAGE);
        Level level = data.get(ComposedDataType.LEVEL);
        DamageSource source = data.get(ComposedDataType.ORIGINAL_SOURCE);
        float particleCount = damage < 1 ? 1 : Math.min(15, damage)/2f;
        AtomicReference<Float> speed = new AtomicReference<>(particleSpeed.orElse(0.35f));
        if(playerOnly){
            if(source.getDirectEntity() != null){
                if(source.getDirectEntity() instanceof Player){
                    if(!applyFilters(entity, (ServerLevel) level, source, particleCount, speed)){
                        spawnParticle((ServerLevel) level, particleOptions, entity, particleCount, 0.35f);
                    }
                }
            } else {
                applyFilters(entity, (ServerLevel) level, source, particleCount, speed);
            }
        } else {
            if (!applyFilters(entity, (ServerLevel) level, source, particleCount, speed)) {
                spawnParticle((ServerLevel) level, particleOptions, entity, particleCount, 0.35f);
            }
        }
    }

    private boolean applyFilters(LivingEntity entity, ServerLevel level, DamageSource source, float particleCount, AtomicReference<Float> speed) {
        entityFilter.ifPresent(particleTagEntityFilters -> particleTagEntityFilters.forEach(filter -> {
            float personalSpeed = filter.getParticleSpeed().orElse(speed.get());
            if (filter.hasEntityType()) {
                if (source.getDirectEntity() == null) return;
                if (filter.getEntityType().get().equals(source.getDirectEntity().getType())) {
                    if (filter.isPlayerOnly() && source.getDirectEntity() instanceof Player) {
                        if ((filter.hasItem() && ((Player) source.getDirectEntity()).getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(filter.getItem().get())) || !filter.hasItem()) {
                            spawnParticle(level, filter.getParticleOptions(), entity, particleCount, personalSpeed);
                        }
                    } else {
                        spawnParticle(level, filter.getParticleOptions(), entity, particleCount, personalSpeed);
                    }
                }
            } else if (filter.hasDamageSource()) {
                if (source.msgId.equals(filter.getDamageSource().get())) {
                    spawnParticle(level, filter.getParticleOptions(), entity, particleCount, personalSpeed);
                } else {
                    spawnParticle(level, particleOptions, entity, particleCount, speed.get());
                }
            } else {
                spawnParticle(level, particleOptions, entity, particleCount, personalSpeed);
            }
        }));
        return entityFilter.isPresent();
    }

    private void spawnParticle(ServerLevel level, ParticleOptions particleOptions, LivingEntity entity, float particleCount, float pSpeed) {
        level.sendParticles(particleOptions, entity.getX(), entity.getY() + entity.getBbHeight() / 2f, entity.getZ(),
                (int) Math.ceil(particleCount * 2),
                0, 0, 0,
                pSpeed);
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
