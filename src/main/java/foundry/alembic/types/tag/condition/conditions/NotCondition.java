package foundry.alembic.types.tag.condition.conditions;

import com.mojang.serialization.Codec;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.types.tag.condition.TagConditionType;
import foundry.alembic.util.ComposedData;

import javax.annotation.Nonnull;
import java.util.List;

public record NotCondition(TagCondition condition) implements TagCondition {
    public static final Codec<NotCondition> CODEC = TagCondition.DISPATCH_CODEC.fieldOf("condition").xmap(
            NotCondition::new,
            NotCondition::condition
    ).codec();

    @Override
    public boolean test(ComposedData composedData) {
        return condition.test(composedData);
    }

    @Nonnull
    @Override
    public TagConditionType<?> getType() {
        return TagConditionType.NOT_CONDITION;
    }
}
