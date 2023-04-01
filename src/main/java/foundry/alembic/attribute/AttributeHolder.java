package foundry.alembic.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.util.CodecUtil;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class AttributeHolder {
    public static final Codec<AttributeHolder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.RANGED_ATTRIBUTE_REGISTRY_CODEC.fieldOf("attribute").forGetter(AttributeHolder::getAttribute),
                    CodecUtil.RANGED_ATTRIBUTE_REGISTRY_CODEC.fieldOf("shielding_attribute").forGetter(attributeHolder -> attributeHolder.shieldingAttribute),
                    CodecUtil.RANGED_ATTRIBUTE_REGISTRY_CODEC.fieldOf("absorption_attribute").forGetter(attributeHolder -> attributeHolder.absorptionAttribute),
                    CodecUtil.RANGED_ATTRIBUTE_REGISTRY_CODEC.fieldOf("resistance_attribute").forGetter(attributeHolder -> attributeHolder.resistanceAttribute)
            ).apply(instance, AttributeHolder::new)
    );

    private RangedAttribute attribute;
    private RangedAttribute shieldingAttribute;
    private RangedAttribute absorptionAttribute;
    private RangedAttribute resistanceAttribute;

    public AttributeHolder(RangedAttribute attribute, RangedAttribute shieldingAttribute, RangedAttribute absorptionAttribute, RangedAttribute resistanceAttribute) {
        this.attribute = attribute;
        this.shieldingAttribute = shieldingAttribute;
        this.absorptionAttribute = absorptionAttribute;
        this.resistanceAttribute = resistanceAttribute;
    }

    public RangedAttribute getAttribute() {
        return attribute;
    }

    public RangedAttribute getShieldingAttribute() {
        return shieldingAttribute;
    }

    public RangedAttribute getAbsorptionAttribute() {
        return absorptionAttribute;
    }

    public RangedAttribute getResistanceAttribute() {
        return resistanceAttribute;
    }
}
