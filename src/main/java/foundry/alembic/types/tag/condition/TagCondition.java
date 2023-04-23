package foundry.alembic.types.tag.condition;

import com.mojang.serialization.Codec;
import foundry.alembic.util.ComposedData;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public interface TagCondition extends Predicate<ComposedData> {
    Codec<TagCondition> CODEC = TagConditionRegistry.CONDITION_LOOKUP_CODEC.dispatch("condition_type", TagCondition::getType, TagConditionType::getCodec);

    @Nonnull
    TagConditionType<?> getType();
}
