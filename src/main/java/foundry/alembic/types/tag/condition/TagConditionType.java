package foundry.alembic.types.tag.condition;

import com.mojang.serialization.Codec;
import foundry.alembic.Alembic;
import foundry.alembic.types.tag.condition.conditions.AllCondition;
import foundry.alembic.types.tag.condition.conditions.DamageSourceTagCondition;
import foundry.alembic.types.tag.condition.conditions.NotCondition;
import foundry.alembic.types.tag.condition.conditions.AnyCondition;

public interface TagConditionType<T extends TagCondition> {
    TagConditionType<NotCondition> NOT_CONDITION = register("not", NotCondition.CODEC);
    TagConditionType<AllCondition> ALL_CONDITION = register("all", AllCondition.CODEC);
    TagConditionType<AnyCondition> ANY_CONDITION = register("any", AnyCondition.CODEC);
    TagConditionType<DamageSourceTagCondition> DAMAGE_SOURCE_CONDITION = register("damage_source", DamageSourceTagCondition.CODEC);
    // TODO: ConditionReference condition. This allows users to put conditions in 'damage_types/tags/conditions' and reference them by resource location

    Codec<T> getCodec();

    static void bootstrap() {}

    private static <T extends TagCondition> TagConditionType<T> register(String id, Codec<T> codec) {
        TagConditionType type = () -> codec;
        TagConditionRegistry.register(Alembic.location(id), type);
        return type;
    }
}
