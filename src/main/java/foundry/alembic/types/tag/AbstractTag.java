package foundry.alembic.types.tag;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.tag.condition.TagCondition;

import java.util.List;

public abstract class AbstractTag implements AlembicTag {
    public static <T extends AbstractTag> Products.P1<RecordCodecBuilder.Mu<T>, List<TagCondition>> createBase(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                TagCondition.REF_OR_LIST.optionalFieldOf("conditions", List.of()).forGetter(AlembicTag::getConditions)
        );
    }

    private final List<TagCondition> conditions;

    public AbstractTag(List<TagCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public List<TagCondition> getConditions() {
        return conditions;
    }
}
