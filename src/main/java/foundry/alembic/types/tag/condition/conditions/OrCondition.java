package foundry.alembic.types.tag.condition.conditions;

import com.mojang.serialization.Codec;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.types.tag.condition.TagConditionType;
import foundry.alembic.util.ComposedData;

import javax.annotation.Nonnull;
import java.util.List;

public record OrCondition(List<TagCondition> conditions) implements TagCondition {
    public static final Codec<OrCondition> CODEC = TagCondition.CODEC.listOf().fieldOf("conditions").xmap(
            OrCondition::new,
            OrCondition::conditions
    ).codec();

    @Override
    public boolean test(ComposedData composedData) {
        return conditions.stream().anyMatch(tagCondition -> tagCondition.test(composedData));
    }

    @Nonnull
    @Override
    public TagConditionType<?> getType() {
        return TagConditionType.OR_CONDITION;
    }
}
