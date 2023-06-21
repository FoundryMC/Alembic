package foundry.alembic.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.util.CodecUtil;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public record AttributeHolder(RangedAttribute attribute, RangedAttribute shieldingAttribute,
                              RangedAttribute absorptionAttribute, RangedAttribute resistanceAttribute) {
    public static final Codec<AttributeHolder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.RANGED_ATTRIBUTE_REGISTRY_CODEC.fieldOf("attribute").forGetter(AttributeHolder::attribute),
                    CodecUtil.RANGED_ATTRIBUTE_REGISTRY_CODEC.fieldOf("shielding_attribute").forGetter(attributeHolder -> attributeHolder.shieldingAttribute),
                    CodecUtil.RANGED_ATTRIBUTE_REGISTRY_CODEC.fieldOf("absorption_attribute").forGetter(attributeHolder -> attributeHolder.absorptionAttribute),
                    CodecUtil.RANGED_ATTRIBUTE_REGISTRY_CODEC.fieldOf("resistance_attribute").forGetter(attributeHolder -> attributeHolder.resistanceAttribute)
            ).apply(instance, AttributeHolder::new)
    );

}
