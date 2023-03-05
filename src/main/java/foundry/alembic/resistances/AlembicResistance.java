package foundry.alembic.resistances;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.AlembicDamageType;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class AlembicResistance {
    public static final Codec<AlembicResistance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Registry.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(AlembicResistance::getEntityType),
                    Codec.INT.fieldOf("priority").forGetter(AlembicResistance::getPriority),
                    Codec.unboundedMap(AlembicDamageType.CODEC, Codec.FLOAT).fieldOf("resistances").forGetter(AlembicResistance::getResistances),
                    Codec.unboundedMap(AlembicDamageType.CODEC, Codec.FLOAT).fieldOf("damage").forGetter(AlembicResistance::getDamage),
                    Codec.STRING.listOf().fieldOf("ignored_sources").xmap(
                            strings -> Util.make((Set<String>)new TreeSet<String>(), set -> set.addAll(strings)),
                            strings -> strings.stream().toList()
                    ).forGetter(alembicResistance -> alembicResistance.ignoredSources)
            ).apply(instance, AlembicResistance::new)
    );

    private EntityType<?> entityType;
    private int priority;
    private ResourceLocation id;
    private Map<AlembicDamageType, Float> resistances;
    private Map<AlembicDamageType, Float> damage;

    private Set<String> ignoredSources;

    public AlembicResistance(EntityType<?> entityType, int priority, Map<AlembicDamageType, Float> resistances, Map<AlembicDamageType, Float> damageTypes, Set<String> ignoredSources) {
        this.entityType = entityType;
        this.priority = priority;
        this.resistances = resistances;
        this.damage = damageTypes;
        this.ignoredSources = ignoredSources;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public Set<String> getIgnoredSources() {
        return ignoredSources;
    }

    public int getPriority() {
        return priority;
    }

    void setId(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public Map<AlembicDamageType, Float> getResistances() {
        return resistances;
    }

    public Map<AlembicDamageType, Float> getDamage() {
        return damage;
    }

    public float getResistance(AlembicDamageType damageType) {
        return resistances.getOrDefault(damageType, 0f);
    }

    public float getDamageType(AlembicDamageType damageType) {
        return damage.getOrDefault(damageType, 0f);
    }
}
