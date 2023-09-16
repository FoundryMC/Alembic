package foundry.alembic.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RangedAttributeData(double base, double min, double max) {
    public static final RangedAttributeData DEFAULT_SHIELDING = new RangedAttributeData(0, 0, 1024);
    public static final RangedAttributeData DEFAULT_ABSORPTION = new RangedAttributeData(0, 0, 1024);
    public static final RangedAttributeData DEFAULT_RESISTANCE = new RangedAttributeData(1, -1024, 1024);
    public static final Codec<RangedAttributeData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("base").forGetter(RangedAttributeData::base),
                    Codec.DOUBLE.fieldOf("min").forGetter(RangedAttributeData::min),
                    Codec.DOUBLE.fieldOf("max").forGetter(RangedAttributeData::max)
            ).apply(instance, RangedAttributeData::new)
    );
}
