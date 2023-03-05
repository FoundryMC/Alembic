package foundry.alembic.types;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import javax.annotation.Nonnull;
import java.util.function.Function;

public enum AlembicTypeModfier implements StringRepresentable {
    RESISTANCE("resistance", AlembicDamageType::getResistanceAttribute),
    SHIELDING("shielding", AlembicDamageType::getShieldAttribute),
    ABSORPTION("absorption", AlembicDamageType::getAbsorptionAttribute);

    public static final Codec<AlembicTypeModfier> CODEC = new StringRepresentable.EnumCodec<>(AlembicTypeModfier.values(), AlembicTypeModfier::valueOf);

    private final String safeName;
    private final Function<AlembicDamageType, RangedAttribute> attributeFunction;

    AlembicTypeModfier(String safeName, Function<AlembicDamageType, RangedAttribute> attributeFunction) {
        this.safeName = safeName;
        this.attributeFunction = attributeFunction;
    }

    public RangedAttribute getAttribute(AlembicDamageType damageType) {
        return attributeFunction.apply(damageType);
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return safeName;
    }
}
