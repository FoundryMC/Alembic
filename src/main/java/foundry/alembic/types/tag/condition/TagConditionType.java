package foundry.alembic.types.tag.condition;

import com.mojang.serialization.Codec;
import foundry.alembic.Alembic;
import foundry.alembic.types.tag.condition.conditions.AndCondition;
import foundry.alembic.types.tag.condition.conditions.DamageSourceTagCondition;
import foundry.alembic.types.tag.condition.conditions.NotCondition;
import foundry.alembic.types.tag.condition.conditions.OrCondition;

public interface TagConditionType<T extends TagCondition> {
    TagConditionType<NotCondition> NOT_CONDITION = register("not", NotCondition.CODEC);
    TagConditionType<AndCondition> AND_CONDITION = register("and", AndCondition.CODEC);
    TagConditionType<OrCondition> OR_CONDITION = register("or", OrCondition.CODEC);
    TagConditionType<DamageSourceTagCondition> DAMAGE_SOURCE_CONDITION = register("damage_source", DamageSourceTagCondition.CODEC);

    Codec<T> getCodec();

    static void bootstrap() {}

    private static <T extends TagCondition> TagConditionType<T> register(String id, Codec<T> codec) {
        TagConditionType type = () -> codec;
        TagConditionRegistry.register(Alembic.location(id), type);
        return type;
    }
}
