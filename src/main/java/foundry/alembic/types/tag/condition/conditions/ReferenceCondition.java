package foundry.alembic.types.tag.condition.conditions;

import com.mojang.serialization.Codec;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.types.tag.condition.TagConditionType;
import foundry.alembic.util.ComposedData;
import foundry.alembic.util.FileReferenceCodec;
import org.jetbrains.annotations.NotNull;

public class ReferenceCondition implements TagCondition {
    private static final String PATH = "alembic/damage_types/conditions/";
    public static final Codec<TagCondition> REFERENCE_CODEC = FileReferenceCodec.json(PATH, TagCondition.DISPATCH_CODEC);
    public static final Codec<ReferenceCondition> CODEC = REFERENCE_CODEC.fieldOf("ref").xmap(
            ReferenceCondition::new,
            referenceCondition -> referenceCondition.reference
    ).codec();

    private final TagCondition reference;

    public ReferenceCondition(TagCondition referencedCondition) {
        this.reference = referencedCondition;
    }

    @NotNull
    @Override
    public TagConditionType<?> getType() {
        return TagConditionType.REFERENCE_CONDITION;
    }

    @Override
    public boolean test(ComposedData composedData) {
        return reference.test(composedData);
    }
}
