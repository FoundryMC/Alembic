package foundry.alembic.types.tag.condition;

import com.mojang.serialization.Codec;
import foundry.alembic.Alembic;
import foundry.alembic.types.tag.condition.conditions.DamageSourceTagCondition;

public interface TagConditionType<T extends TagCondition> {
    TagConditionType<DamageSourceTagCondition> DAMAGE_SOURCE_CONDITION = register("damage_source", DamageSourceTagCondition.CODEC);

    Codec<T> getCodec();

    static void bootstrap() {}

    private static <T extends TagCondition> TagConditionType<T> register(String id, Codec<T> codec) {
        TagConditionType type = () -> codec;
        TagConditionRegistry.register(Alembic.location(id), type);
        return type;
    }
}
