package foundry.alembic.types;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import javax.annotation.Nonnull;
import java.util.function.Function;

public enum AlembicTypeModifier implements StringRepresentable {
    RESISTANCE("resistance", AlembicDamageType::getResistanceAttribute),
    SHIELDING("shielding", AlembicDamageType::getShieldAttribute),
    ABSORPTION("absorption", AlembicDamageType::getAbsorptionAttribute);

    public static final Codec<AlembicTypeModifier> CODEC = StringRepresentable.fromEnum(AlembicTypeModifier::values);

    private final String safeName;
    private final Function<AlembicDamageType, RangedAttribute> attributeFunction;

    AlembicTypeModifier(String safeName, Function<AlembicDamageType, RangedAttribute> attributeFunction) {
        this.safeName = safeName;
        this.attributeFunction = attributeFunction;
    }

    public RangedAttribute getAffectedAttribute(AlembicDamageType damageType) {
        return attributeFunction.apply(damageType);
    }

    public String getId(AlembicDamageType damageType) {
        return damageType.getId().getPath() + "_" + safeName;
    }

    public String getId(String someString) {
        return someString + "_" + safeName;
    }

    public static AlembicTypeModifier[] getMatching(String attributeMod) {
        switch (attributeMod) {
            case "resistance" -> {
                return new AlembicTypeModifier[] {RESISTANCE};
            }
            case "shielding" -> {
                return new AlembicTypeModifier[] {SHIELDING};
            }
            case "absorption" -> {
                return new AlembicTypeModifier[] {ABSORPTION};
            }
            case "all" -> {
                return values();
            }
            default -> {
                return new AlembicTypeModifier[0];
            }
        }
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return safeName;
    }
}
