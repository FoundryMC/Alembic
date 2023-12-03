package foundry.alembic.stats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.DamageTypeManager;
import foundry.alembic.util.CodecUtil;
import foundry.alembic.damagesource.DamageSourceIdentifier;
import foundry.alembic.types.AlembicDamageType;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.Set;

public class AlembicEntityStats {
    public static final Codec<AlembicEntityStats> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(AlembicEntityStats::getEntityType),
                    Codec.INT.fieldOf("priority").forGetter(AlembicEntityStats::getPriority),
                    Codec.unboundedMap(CodecUtil.ALEMBIC_RL_CODEC, Codec.FLOAT).flatXmap(
                            map -> {
                                Object2FloatMap<AlembicDamageType> retMap = new Object2FloatOpenHashMap<>();
                                for (Map.Entry<ResourceLocation, Float> entry : map.entrySet()) {
                                    if (!DamageTypeManager.containsKey(entry.getKey())) {
                                        return DataResult.error(() -> "Damage type %s does not exist!".formatted(entry.getKey()));
                                    }
                                    retMap.put(DamageTypeManager.getDamageType(entry.getKey()), entry.getValue());
                                }
                                return DataResult.success(retMap);
                            },
                            resistance -> {
                                Object2FloatMap<ResourceLocation> retMap = new Object2FloatOpenHashMap<>();
                                for (Map.Entry<AlembicDamageType, Float> entry : resistance.object2FloatEntrySet()) {
                                    retMap.put(entry.getKey().getId(), entry.getValue());
                                }
                                return DataResult.success(retMap);
                            }
                    ).fieldOf("resistances").forGetter(AlembicEntityStats::getResistances),
                    Codec.unboundedMap(CodecUtil.ALEMBIC_RL_CODEC, Codec.FLOAT).flatXmap(
                            map -> {
                                Object2FloatMap<AlembicDamageType> retMap = new Object2FloatOpenHashMap<>();
                                for (Map.Entry<ResourceLocation, Float> entry : map.entrySet()) {
                                    if (!DamageTypeManager.containsKey(entry.getKey())) {
                                        return DataResult.error(() -> "Damage type %s does not exist!".formatted(entry.getKey()));
                                    }
                                    retMap.put(DamageTypeManager.getDamageType(entry.getKey()), entry.getValue());
                                }
                                return DataResult.success(retMap);
                            },
                            damage -> {
                                Object2FloatMap<ResourceLocation> retMap = new Object2FloatOpenHashMap<>();
                                for (Map.Entry<AlembicDamageType, Float> entry : damage.object2FloatEntrySet()) {
                                    retMap.put(entry.getKey().getId(), entry.getValue());
                                }
                                return DataResult.success(retMap);
                            }
                    ).fieldOf("damage").forGetter(AlembicEntityStats::getDamage),
                    CodecUtil.setOf(DamageSourceIdentifier.CODEC).fieldOf("ignored_sources").forGetter(alembicResistance -> alembicResistance.ignoredSources)
            ).apply(instance, AlembicEntityStats::new)
    );

    private EntityType<?> entityType;
    private int priority;
    private ResourceLocation id;
    private Object2FloatMap<AlembicDamageType> resistances;
    private Object2FloatMap<AlembicDamageType> damage;

    private Set<DamageSourceIdentifier> ignoredSources;

    public AlembicEntityStats(EntityType<?> entityType, int priority, Object2FloatMap<AlembicDamageType> resistances, Object2FloatMap<AlembicDamageType> damageTypes, Set<DamageSourceIdentifier> ignoredSources) {
        this.entityType = entityType;
        this.priority = priority;
        this.resistances = resistances;
        this.damage = damageTypes;
        this.ignoredSources = ignoredSources;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public Set<DamageSourceIdentifier> getIgnoredSources() {
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

    public Object2FloatMap<AlembicDamageType> getResistances() {
        return resistances;
    }

    public Object2FloatMap<AlembicDamageType> getDamage() {
        return damage;
    }

    public float getResistance(AlembicDamageType damageType) {
        return resistances.getOrDefault(damageType, 0f);
    }

    public float getDamageType(AlembicDamageType damageType) {
        return damage.getOrDefault(damageType, 0f);
    }
}
