package foundry.alembic.types.tag.condition.conditions;

import com.mojang.serialization.Codec;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.types.tag.condition.TagConditionType;
import foundry.alembic.util.ComposedData;

import javax.annotation.Nonnull;
import java.util.List;

public record AnyCondition(List<TagCondition> conditions) implements TagCondition {
    public static final Codec<AnyCondition> CODEC = TagCondition.DISPATCH_CODEC.listOf().fieldOf("conditions").xmap(
            AnyCondition::new,
            AnyCondition::conditions
    ).codec();

    @Override
    public boolean test(ComposedData composedData) {
        return conditions.stream().anyMatch(tagCondition -> tagCondition.test(composedData));
    }

    @Nonnull
    @Override
    public TagConditionType<?> getType() {
        return TagConditionType.ANY_CONDITION;
    }
}
