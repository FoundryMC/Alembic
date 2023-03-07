package foundry.alembic.types;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class AlembicAttribute extends RangedAttribute {

    public AlembicAttribute(String descriptionId, double defaultValue, double min, double max) {
        super(descriptionId, defaultValue, min, max);
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
