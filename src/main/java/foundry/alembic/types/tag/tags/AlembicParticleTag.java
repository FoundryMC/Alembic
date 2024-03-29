package foundry.alembic.types.tag.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.tag.AbstractTag;
import foundry.alembic.types.tag.AlembicTagType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.util.ComposedData;
import foundry.alembic.util.ComposedDataTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlembicParticleTag extends AbstractTag {
    public static final Codec<AlembicParticleTag> CODEC = RecordCodecBuilder.create(instance ->
        createBase(instance).and(
                instance.group(
                        ParticleTypes.CODEC.fieldOf("particle_options").forGetter(alembicParticleTag -> alembicParticleTag.particleOptions),
                        Codec.FLOAT.optionalFieldOf("particle_speed", 0.35f).forGetter(alembicParticleTag -> alembicParticleTag.particleSpeed),
                        Codec.BOOL.optionalFieldOf("scale_with_damage", false).forGetter(alembicParticleTag -> alembicParticleTag.scaleWithDamage),
                        Codec.FLOAT.optionalFieldOf("scalar", 0.5f).forGetter(alembicParticleTag -> alembicParticleTag.scalar)
                )
        ).apply(instance, AlembicParticleTag::new)
    );
    private final ParticleOptions particleOptions;
    private final float particleSpeed;
    private final boolean scaleWithDamage;
    private final float scalar;
    public AlembicParticleTag(List<TagCondition> conditions, ParticleOptions particleOptions, float particleSpeed, boolean scaleWithDamage, float scalar) {
        super(conditions);
        this.particleOptions = particleOptions;
        this.particleSpeed = particleSpeed;
        this.scaleWithDamage = scaleWithDamage;
        this.scalar = scalar;
    }

    public AlembicParticleTag(ParticleOptions particleOptions) {
        this(List.of(), particleOptions, 0.35f, false, 0.5f);
    }


    @Override
    public void onDamage(ComposedData data) {
        LivingEntity entity = data.get(ComposedDataTypes.TARGET_ENTITY);
        float damage = data.get(ComposedDataTypes.FINAL_DAMAGE);
        ServerLevel level = data.get(ComposedDataTypes.SERVER_LEVEL);
        float particleCount = damage < 1 ? 1 : Math.min(15, damage)/2f;
        if(!scaleWithDamage) particleCount = 1;
        particleCount = (int) Math.floor(particleCount * scalar);
        spawnParticle(level, particleOptions, entity, particleCount, particleSpeed);
    }

    private void spawnParticle(ServerLevel level, ParticleOptions particleOptions, LivingEntity entity, float particleCount, float pSpeed) {
        ForgeRegistries.PARTICLE_TYPES.getKey(particleOptions.getType());
        // random offset between -0.1 and 0.1
        Vec3 randomOffset = new Vec3(
                (level.random.nextFloat() - 1f) * 0.5f,
                (level.random.nextFloat() - 1f) * 0.5f,
                (level.random.nextFloat() - 1f) * 0.5f
        );
        level.sendParticles(particleOptions, entity.getX(), entity.getY(0.5D), entity.getZ(),
                (int) Math.ceil(particleCount),
                randomOffset.x, 0, randomOffset.z,
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
