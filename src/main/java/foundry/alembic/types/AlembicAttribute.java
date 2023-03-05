package foundry.alembic.types;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class AlembicAttribute extends RangedAttribute {

    public AlembicAttribute(String descriptionId, double defaultValue, double min, double max) {
        super(descriptionId, min, max, defaultValue);
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
