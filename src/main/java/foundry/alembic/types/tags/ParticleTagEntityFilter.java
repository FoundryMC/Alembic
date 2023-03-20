package foundry.alembic.types.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.Optional;

public class ParticleTagEntityFilter {
    public static final Codec<ParticleTagEntityFilter> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("player_only").forGetter(particleTagEntityFilter -> particleTagEntityFilter.playerOnly),
                    Registry.ENTITY_TYPE.byNameCodec().optionalFieldOf("entity_type").forGetter(particleTagEntityFilter -> particleTagEntityFilter.entityType),
                    ParticleTypes.CODEC.fieldOf("particle_options").forGetter(particleTagEntityFilter -> particleTagEntityFilter.particleOptions),
                    Registry.ITEM.byNameCodec().optionalFieldOf("item").forGetter(particleTagEntityFilter -> particleTagEntityFilter.item),
                    Codec.STRING.optionalFieldOf("damage_source").forGetter(particleTagEntityFilter -> particleTagEntityFilter.damageSource),
                    Codec.FLOAT.optionalFieldOf("particle_speed").forGetter(particleTagEntityFilter -> particleTagEntityFilter.particleSpeed)
            ).apply(instance, ParticleTagEntityFilter::new)
    );

    private final boolean playerOnly;
    private final Optional<EntityType<?>> entityType;
    private final Optional<String> damageSource;
    private final Optional<Float> particleSpeed;
    private final ParticleOptions particleOptions;

    private final Optional<Item> item;


    public ParticleTagEntityFilter(boolean playerOnly, Optional<EntityType<?>> entityType, ParticleOptions particleOptions, Optional<Item> item, Optional<String> damageSource, Optional<Float> particleSpeed) {
        this.playerOnly = playerOnly;
        this.entityType = entityType;
        this.particleOptions = particleOptions;
        this.item = item;
        this.damageSource = damageSource;
        this.particleSpeed = particleSpeed;
    }

    public boolean isPlayerOnly() {
        return playerOnly;
    }

    public Optional<EntityType<?>> getEntityType() {
        return entityType;
    }

    public ParticleOptions getParticleOptions() {
        return particleOptions;
    }

    public Optional<Item> getItem() {
        return item;
    }

    public boolean hasItem() {
        return item.isPresent();
    }

    public boolean hasEntityType() {
        return entityType.isPresent();
    }

    public boolean hasDamageSource() {
        return damageSource.isPresent();
    }

    public Optional<String> getDamageSource() {
        return damageSource;
    }

    public Optional<Float> getParticleSpeed() {
        return particleSpeed;
    }

    public boolean hasParticleSpeed() {
        return particleSpeed.isPresent();
    }
}
