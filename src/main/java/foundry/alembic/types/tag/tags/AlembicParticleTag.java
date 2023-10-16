package foundry.alembic.types.tag.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.tag.AbstractTag;
import foundry.alembic.types.tag.AlembicTagType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.util.ComposedData;
import foundry.alembic.util.ComposedDataTypes;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AlembicParticleTag extends AbstractTag {
    public static final Codec<AlembicParticleTag> CODEC = RecordCodecBuilder.create(instance ->
        createBase(instance).and(
                instance.group(
                        ParticleTypes.CODEC.fieldOf("particle_options").forGetter(alembicParticleTag -> alembicParticleTag.particleOptions),
                        Codec.FLOAT.optionalFieldOf("particle_speed", 0.35f).forGetter(alembicParticleTag -> alembicParticleTag.particleSpeed)
                )
        ).apply(instance, AlembicParticleTag::new)
    );
    private final ParticleOptions particleOptions;
    private final float particleSpeed;
    public AlembicParticleTag(List<TagCondition> conditions, ParticleOptions particleOptions, float particleSpeed) {
        super(conditions);
        this.particleOptions = particleOptions;
        this.particleSpeed = particleSpeed;
    }

    public AlembicParticleTag(ParticleOptions particleOptions) {
        this(List.of(), particleOptions, 0.35f);
    }


    @Override
    public void onDamage(ComposedData data) {
        LivingEntity entity = data.get(ComposedDataTypes.TARGET_ENTITY);
        float damage = data.get(ComposedDataTypes.FINAL_DAMAGE);
        ServerLevel level = data.get(ComposedDataTypes.SERVER_LEVEL);
        float particleCount = damage < 1 ? 1 : Math.min(15, damage)/2f;
        spawnParticle(level, particleOptions, entity, particleCount, particleSpeed);
    }

    private void spawnParticle(ServerLevel level, ParticleOptions particleOptions, LivingEntity entity, float particleCount, float pSpeed) {
        ForgeRegistries.PARTICLE_TYPES.getKey(particleOptions.getType());
        level.sendParticles(particleOptions, entity.getX(), entity.getY() + entity.getBbHeight() / 2f, entity.getZ(),
                (int) Math.ceil(particleCount * 2),
                0, 0, 0,
                pSpeed);
    }

    @Override
    public @NotNull AlembicTagType<?> getType() {
        return AlembicTagType.PARTICLE;
    }

    @Override
    public String toString() {
        return "AlembicParticleTag, Particle options: " + particleOptions.getType().getClass().getSimpleName();
    }
}
