package foundry.alembic.types.tag.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.tag.AbstractTag;
import foundry.alembic.types.tag.AlembicTagType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.codecs.CodecUtil;
import foundry.alembic.util.ComposedData;
import foundry.alembic.util.ComposedDataTypes;
import foundry.alembic.util.TagOrElements;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AlembicExtendFireTag extends AbstractTag {
    public static final Codec<AlembicExtendFireTag> CODEC = RecordCodecBuilder.create(instance ->
            createBase(instance).and(
                    instance.group(
                            Codec.FLOAT.fieldOf("multiplier").forGetter(alembicExtendFireTag -> alembicExtendFireTag.multiplier),
                            CodecUtil.setOf(TagOrElements.lazyCodec(Registries.DAMAGE_TYPE)).fieldOf("ignored_sources").forGetter(alembicExtendFireTag -> alembicExtendFireTag.ignoredDamageTypesRaw)
                    )
            ).apply(instance, AlembicExtendFireTag::new)
    );

    private final float multiplier;
    private final Set<TagOrElements.Lazy<DamageType>> ignoredDamageTypesRaw;
    private Set<DamageType> ignoredDamageTypes;

    public AlembicExtendFireTag(List<TagCondition> conditions, float multiplier, Set<TagOrElements.Lazy<DamageType>> ignoredDamageTypesRaw) {
        super(conditions);
        this.multiplier = multiplier;
        this.ignoredDamageTypesRaw = ignoredDamageTypesRaw;
    }

    @Override
    public void onDamage(ComposedData data) {
        if (ignoredDamageTypes == null) {
            ignoredDamageTypes = ignoredDamageTypesRaw.stream().flatMap(damageTypeLazy -> damageTypeLazy.getElements(data.get(ComposedDataTypes.SERVER_LEVEL).registryAccess()).stream()).collect(Collectors.toSet());
        }

        LivingEntity entity = data.get(ComposedDataTypes.TARGET_ENTITY);
        float damage = data.get(ComposedDataTypes.FINAL_DAMAGE);
        DamageSource originalSource = data.get(ComposedDataTypes.ORIGINAL_SOURCE);

        if(entity.isOnFire() && !ignoredDamageTypes.contains(originalSource.type())) {
            entity.setSecondsOnFire((entity.getRemainingFireTicks()/20) + (int)Math.ceil((damage*multiplier)));
        }
    }

    @Override
    public @NotNull AlembicTagType<?> getType() {
        return AlembicTagType.EXTEND_FIRE;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " Multiplier: %s, Ignored sources: %s".formatted(multiplier, Arrays.toString(ignoredDamageTypes.toArray()));
    }
}
