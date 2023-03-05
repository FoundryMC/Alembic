package foundry.alembic.resistances;

import foundry.alembic.types.AlembicDamageType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Map;

public class AlembicResistance {
    private EntityType<?> entityType;
    private int priority;
    private ResourceLocation id;
    private Map<AlembicDamageType, Float> resistances;
    private Map<AlembicDamageType, Float> damage;

    private List<String> ignoredSources;

    public AlembicResistance(EntityType<?> entityType, int priority, ResourceLocation id, Map<AlembicDamageType, Float> resistances, Map<AlembicDamageType, Float> damageTypes, List<String> ignoredSources) {
        this.entityType = entityType;
        this.priority = priority;
        this.id = id;
        this.resistances = resistances;
        this.damage = damageTypes;
        this.ignoredSources = ignoredSources;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public List<String> getIgnoredSources() {
        return ignoredSources;
    }

    public int getPriority() {
        return priority;
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
