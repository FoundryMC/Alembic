package foundry.alembic.types.tag.condition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import foundry.alembic.types.tag.condition.conditions.ReferenceCondition;
import foundry.alembic.util.ComposedData;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface TagCondition extends Predicate<ComposedData> {
    Codec<TagCondition> DISPATCH_CODEC = TagConditionRegistry.CONDITION_LOOKUP_CODEC.dispatch("condition_type", TagCondition::getType, TagConditionType::getCodec);
    Codec<List<TagCondition>> REF_OR_LIST = Codec.either(ReferenceCondition.REFERENCE_CODEC, DISPATCH_CODEC.listOf())
            .xmap(
                    either -> either.map(List::of, Function.identity()),
                    conditions -> conditions.size() == 1 && conditions.get(0) instanceof ReferenceCondition ? Either.left(conditions.get(0)) : Either.right(conditions)
            );

    @Nonnull
    TagConditionType<?> getType();
}
