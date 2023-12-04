package foundry.alembic.types.tag.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.tag.AbstractTag;
import foundry.alembic.types.tag.AlembicTag;
import foundry.alembic.types.tag.AlembicTagType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.util.FileReferenceCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlembicReferenceTag extends AbstractTag {
    public static final Codec<AlembicTag> REFERENCE_CODEC = FileReferenceCodec.json("alembic/damage_types/tags/", AlembicTag.DISPATCH_CODEC);
    public static final Codec<AlembicReferenceTag> CODEC = RecordCodecBuilder.create(instance ->
            createBase(instance).and(
                    REFERENCE_CODEC.fieldOf("ref").forGetter(tag -> tag.referencedTag)
            ).apply(instance, AlembicReferenceTag::new)
    );

    private final AlembicTag referencedTag;

    public AlembicReferenceTag(List<TagCondition> conditions, AlembicTag referencedTag) {
        super(conditions);
        this.referencedTag = referencedTag;
    }

    @NotNull
    @Override
    public AlembicTagType<?> getType() {
        return AlembicTagType.REFERENCE;
    }
}
