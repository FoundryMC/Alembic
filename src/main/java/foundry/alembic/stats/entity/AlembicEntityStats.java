package foundry.alembic.stats.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeManager;
import foundry.alembic.codecs.CodecUtil;
import foundry.alembic.util.TagOrElements;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class AlembicEntityStats {
    public static final Codec<AlembicEntityStats> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(AlembicEntityStats::getEntityType),
                    Codec.INT.fieldOf("priority").forGetter(AlembicEntityStats::getPriority),
                    Codec.unboundedMap(DamageTypeManager.DAMAGE_TYPE_CODEC, Codec.FLOAT).comapFlatMap(
                            map -> {
                                if (map.isEmpty()) {
                                    return DataResult.error(() -> "Must have entries under \"resistances\"");
                                }
                                Reference2FloatMap<AlembicDamageType> retMap = new Reference2FloatOpenHashMap<>(map);
                                return DataResult.success(retMap);
                            },
                            Function.identity()
                    ).fieldOf("resistances").forGetter(AlembicEntityStats::getResistances),
                    Codec.unboundedMap(DamageTypeManager.DAMAGE_TYPE_CODEC, Codec.FLOAT).comapFlatMap(
                            map -> {
                                if (map.isEmpty()) {
                                    return DataResult.error(() -> "Must have entries under \"damage\"");
                                }
                                Reference2FloatMap<AlembicDamageType> retMap = new Reference2FloatOpenHashMap<>(map);
                                return DataResult.success(retMap);
                            },
                            Function.identity()
                    ).fieldOf("damage").forGetter(AlembicEntityStats::getDamage),
                    CodecUtil.setOf(TagOrElements.lazyCodec(Registries.DAMAGE_TYPE)).fieldOf("ignored_sources").forGetter(alembicResistance -> alembicResistance.ignoredSourcesRaw)
            ).apply(instance, AlembicEntityStats::new)
    );

    private final EntityType<?> entityType;
    private final int priority;
    private ResourceLocation id;
    private final Reference2FloatMap<AlembicDamageType> resistances;
    private final Reference2FloatMap<AlembicDamageType> damage;

    private final Set<TagOrElements.Lazy<DamageType>> ignoredSourcesRaw;

    public AlembicEntityStats(EntityType<?> entityType, int priority, Reference2FloatMap<AlembicDamageType> resistances, Reference2FloatMap<AlembicDamageType> damageTypes, Set<TagOrElements.Lazy<DamageType>> ignoredSourcesRaw) {
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

    public Reference2FloatMap<AlembicDamageType> getResistances() {
        return resistances;
    }

    public Reference2FloatMap<AlembicDamageType> getDamage() {
        return damage;
    }

    public float getResistance(AlembicDamageType damageType) {
        return resistances.getOrDefault(damageType, 0f);
    }

    public float getDamageType(AlembicDamageType damageType) {
        return damage.getOrDefault(damageType, 0f);
    }
}
