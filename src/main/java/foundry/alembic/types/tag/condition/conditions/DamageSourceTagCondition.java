package foundry.alembic.types.tag.condition.conditions;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.types.tag.condition.TagConditionType;
import foundry.alembic.types.tag.condition.predicates.DamageSourcePredicate;
import foundry.alembic.util.ComposedData;
import foundry.alembic.util.ComposedDataTypes;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public class DamageSourceTagCondition implements TagCondition {
    public static final Codec<DamageSourceTagCondition> CODEC = DamageSourcePredicate.CODEC.xmap(
            DamageSourceTagCondition::new,
            damageSourceTagCondition -> damageSourceTagCondition.damageSourcePredicate
    ).codec();

    private final DamageSourcePredicate damageSourcePredicate;

    public DamageSourceTagCondition(DamageSourcePredicate damageSourcePredicate) {
        this.damageSourcePredicate = damageSourcePredicate;
    }

    @Override
    public boolean test(ComposedData composedData) {
        return damageSourcePredicate.matches(composedData.get(ComposedDataTypes.ORIGINAL_SOURCE));
    }

    @Nonnull
    @Override
    public TagConditionType<?> getType() {
        return TagConditionType.DAMAGE_SOURCE_CONDITION;
    }
}
