package foundry.alembic.types.tag.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.tag.AbstractTag;
import foundry.alembic.types.tag.AlembicTag;
import foundry.alembic.types.tag.AlembicTagType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.util.ComposedData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AlembicConditionalTag extends AbstractTag {
    public static final Codec<AlembicConditionalTag> CODEC = RecordCodecBuilder.create(instance ->
            AbstractTag.createBase(instance).and(
                    instance.group(
                            AlembicTag.DISPATCH_CODEC.fieldOf("run").forGetter(sequence -> sequence.run),
                            AlembicTag.DISPATCH_CODEC.fieldOf("else_run").forGetter(sequence -> sequence.elseRun)
                    )
            ).apply(instance, AlembicConditionalTag::new)
    );

    private final AlembicTag run;
    private final AlembicTag elseRun;

    public AlembicConditionalTag(Set<TagCondition> conditions, AlembicTag run, AlembicTag elseRun) {
        super(conditions);
        this.run = run;
        this.elseRun = elseRun;
    }

    @Override
    public void onDamage(ComposedData data) {
        if (run.testConditions(data)) {
            run.onDamage(data);
        } else {
            if (elseRun.testConditions(data)) {
                elseRun.onDamage(data);
            }
        }
    }

    @NotNull
    @Override
    public AlembicTagType<?> getType() {
        return AlembicTagType.CONDITIONAL;
    }
}
