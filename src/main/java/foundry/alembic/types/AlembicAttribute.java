package foundry.alembic.types;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class AlembicAttribute extends RangedAttribute {

    public AlembicAttribute(String p_22310_, double p_22311_, double p_22312_, double p_22313_) {
        super(p_22310_, p_22311_, p_22312_, p_22313_);
    }

    public void setBaseValue(double value){
        this.defaultValue = value;
    }

    public void setMinValue(double value){
        this.minValue = value;
    }

    public void setMaxValue(double value){
        this.maxValue = value;
    }

    public void setDescriptionId(String id){
        this.descriptionId = id;
    }

}
