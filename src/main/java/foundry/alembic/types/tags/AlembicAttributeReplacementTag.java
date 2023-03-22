package foundry.alembic.types.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class AlembicAttributeReplacementTag implements AlembicTag {
    public static final Codec<AlembicAttributeReplacementTag> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Registry.ATTRIBUTE.byNameCodec().fieldOf("damage_type").forGetter(alembicAttributeReplacementTag -> alembicAttributeReplacementTag.attributeToReplace),
                    Registry.ATTRIBUTE.byNameCodec().fieldOf("attribute_to_replace_with").forGetter(alembicAttributeReplacementTag -> alembicAttributeReplacementTag.attributeToReplaceWith),
                    Codec.STRING.fieldOf("attribute_string").forGetter(alembicAttributeReplacementTag -> alembicAttributeReplacementTag.attributeString)
            ).apply(instance, AlembicAttributeReplacementTag::new)
    );

    private final Attribute attributeToReplace;
    private final Attribute attributeToReplaceWith;
    private final String attributeString;

    public AlembicAttributeReplacementTag(Attribute attributeToReplace, Attribute attributeToReplaceWith, String attributeString) {
        this.attributeToReplace = attributeToReplace;
        this.attributeToReplaceWith = attributeToReplaceWith;
        this.attributeString = attributeString;
    }
    @Override
    public void run(ComposedData data) {

    }

    @Override
    public AlembicTagType<?> getType() {
        return null;
    }

    @Override
    public void handlePostParse(AlembicDamageType damageType) {
        DamageTypeRegistry.getDamageTypes().forEach(alembicDamageType -> {
            if (alembicDamageType.getAttribute().equals(attributeToReplace)) {
                alembicDamageType.setAttribute(attributeString, (RangedAttribute) attributeToReplaceWith);
            }
        });
    }
}
