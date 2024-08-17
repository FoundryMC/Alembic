package foundry.alembic.stats.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.codecs.SetCodec;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeManager;
import foundry.alembic.codecs.CodecUtil;
import foundry.alembic.util.TagOrElements;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AlembicEntityStats {
    public static Codec<AlembicEntityStats> codec(ICondition.IContext context) {
        return RecordCodecBuilder.create(instance ->
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
                        CodecUtil.setOf(TagOrElements.codec(Registries.DAMAGE_TYPE, context)).fieldOf("ignored_sources").forGetter(alembicResistance -> alembicResistance.ignoredSourcesRaw)
                ).apply(instance, AlembicEntityStats::new)
        );
    }

    private final EntityType<?> entityType;
    private final int priority;
    private final Reference2FloatMap<AlembicDamageType> resistances;
    private final Reference2FloatMap<AlembicDamageType> damage;
    private final Set<Holder<DamageType>> ignoredSources;

    private final Set<TagOrElements.Immediate<DamageType>> ignoredSourcesRaw;

    public AlembicEntityStats(EntityType<?> entityType, int priority, Reference2FloatMap<AlembicDamageType> resistances, Reference2FloatMap<AlembicDamageType> damageTypes, Set<TagOrElements.Immediate<DamageType>> ignoredSources) {
        this.entityType = entityType;
        this.priority = priority;
        this.resistances = resistances;
        this.damage = damageTypes;
        this.ignoredSourcesRaw = ignoredSources;
        this.ignoredSources = ignoredSources.stream().flatMap(damageTypeImmediate -> damageTypeImmediate.getElements().stream()).collect(Collectors.toSet());
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public boolean isDamageIgnored(DamageSource damageSource) {
        return ignoredSources.contains(damageSource.typeHolder());
    }

    public int getPriority() {
        return priority;
    }

    public Reference2FloatMap<AlembicDamageType> getResistances() {
        return resistances;
    }

    public Reference2FloatMap<AlembicDamageType> getDamage() {
        return damage;
    }

    public float getResistance(AlembicDamageType damageType) {
        return resistances.getFloat(damageType);
    }

    public float getDamageType(AlembicDamageType damageType) {
        return damage.getFloat(damageType);
    }
}
