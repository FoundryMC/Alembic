package foundry.alembic.types.tag.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.damagesource.DamageSourceIdentifier;
import foundry.alembic.types.tag.AbstractTag;
import foundry.alembic.types.tag.AlembicTagType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.util.CodecUtil;
import foundry.alembic.util.ComposedData;
import foundry.alembic.util.ComposedDataTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

public class AlembicExtendFireTag extends AbstractTag {
    public static final Codec<AlembicExtendFireTag> CODEC = RecordCodecBuilder.create(instance ->
            createBase(instance).and(
                    instance.group(
                            Codec.FLOAT.fieldOf("multiplier").forGetter(alembicExtendFireTag -> alembicExtendFireTag.multiplier),
                            CodecUtil.setOf(DamageSourceIdentifier.CODEC).fieldOf("ignored_sources").forGetter(alembicExtendFireTag -> alembicExtendFireTag.ignoredSources)
                    )
            ).apply(instance, AlembicExtendFireTag::new)
    );

    private final float multiplier;
    private final Set<DamageSourceIdentifier> ignoredSources;

    public AlembicExtendFireTag(Set<TagCondition> conditions, float multiplier, Set<DamageSourceIdentifier> ignoredSources) {
        super(conditions);
        this.multiplier = multiplier;
        this.ignoredSources = ignoredSources;
    }
    @Override
    public void onDamage(ComposedData data) {
        LivingEntity entity = data.get(ComposedDataTypes.TARGET_ENTITY);
        float damage = data.get(ComposedDataTypes.FINAL_DAMAGE);
        DamageSource originalSource = data.get(ComposedDataTypes.ORIGINAL_SOURCE);
        if(entity.isOnFire() && !ignoredSources.contains(DamageSourceIdentifier.create(originalSource.msgId))){
            entity.setSecondsOnFire((entity.getRemainingFireTicks()/20) + (int)Math.ceil((damage*multiplier)));
        }
    }

    @Override
    public @NotNull AlembicTagType<?> getType() {
        return AlembicTagType.EXTEND_FIRE;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " Multiplier: %s, Ignored sources: %s".formatted(multiplier, Arrays.toString(ignoredSources.toArray()));
    }
}
