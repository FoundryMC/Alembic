package foundry.alembic.stats.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeManager;
import foundry.alembic.util.CodecUtil;
import foundry.alembic.util.TagOrElements;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
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
                    CodecUtil.setOf(TagOrElements.lazyCodec(Registries.DAMAGE_TYPE)).fieldOf("ignored_sources").forGetter(alembicResistance -> alembicResistance.ignoredSourcesRaw)
            ).apply(instance, AlembicEntityStats::new)
    );

    private final EntityType<?> entityType;
    private final int priority;
    private ResourceLocation id;
    private final Object2FloatMap<AlembicDamageType> resistances;
    private final Object2FloatMap<AlembicDamageType> damage;

    private final Set<TagOrElements.Lazy<DamageType>> ignoredSourcesRaw;

    public AlembicEntityStats(EntityType<?> entityType, int priority, Object2FloatMap<AlembicDamageType> resistances, Object2FloatMap<AlembicDamageType> damageTypes, Set<TagOrElements.Lazy<DamageType>> ignoredSourcesRaw) {
        this.entityType = entityType;
        this.priority = priority;
        this.resistances = resistances;
        this.damage = damageTypes;
        this.ignoredSourcesRaw = ignoredSourcesRaw;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public Set<TagOrElements.Lazy<DamageType>> getIgnoredSources() {
        return ignoredSourcesRaw;
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
