package foundry.alembic.types.tag.condition.conditions;

import com.mojang.serialization.Codec;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.types.tag.condition.TagConditionType;
import foundry.alembic.util.ComposedData;

import javax.annotation.Nonnull;
import java.util.List;

public record AndCondition(List<TagCondition> conditions) implements TagCondition {
    public static final Codec<AndCondition> CODEC = TagCondition.CODEC.listOf().fieldOf("conditions").xmap(
            AndCondition::new,
            AndCondition::conditions
    ).codec();

    @Override
    public boolean test(ComposedData composedData) {
        return conditions.stream().allMatch(tagCondition -> tagCondition.test(composedData));
    }

    @Nonnull
    @Override
    public TagConditionType<?> getType() {
        return TagConditionType.AND_CONDITION;
    }
}
