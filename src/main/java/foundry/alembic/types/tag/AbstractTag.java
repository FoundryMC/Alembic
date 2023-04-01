package foundry.alembic.types.tag;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.util.CodecUtil;

import java.util.Set;

public abstract class AbstractTag implements AlembicTag {
    public static <T extends AbstractTag> Products.P1<RecordCodecBuilder.Mu<T>, Set<TagCondition>> createBase(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                CodecUtil.setOf(TagCondition.CODEC).optionalFieldOf("conditions", Set.of()).forGetter(AlembicTag::getConditions)
        );
    }

    private final Set<TagCondition> conditions;

    public AbstractTag(Set<TagCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public Set<TagCondition> getConditions() {
        return conditions;
    }
}
