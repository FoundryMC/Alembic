package foundry.alembic.types;

import com.mojang.serialization.Codec;
import foundry.alembic.potion.PotionModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import javax.annotation.Nonnull;
import java.util.function.Function;

public enum AlembicTypeModifier implements StringRepresentable {
    SHIELDING("shielding", AlembicDamageType::getShieldingAttribute),
    ABSORPTION("absorption", AlembicDamageType::getAbsorptionAttribute),
    RESISTANCE("resistance", AlembicDamageType::getResistanceAttribute);

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

    public ResourceLocation computePotionId(ResourceLocation attributeSetId) {
        return new ResourceLocation(attributeSetId.getNamespace(), attributeSetId.getPath() + "_" + safeName);
    }

    public ResourceLocation computePotionId(ResourceLocation attributeSetId, PotionModifier potionModifier) {
        return new ResourceLocation(attributeSetId.getNamespace(), potionModifier.getSerializedName() + "_" + attributeSetId.getPath() + "_" + safeName);
    }

    public ResourceLocation computeAttributeId(ResourceLocation baseRl) {
        return new ResourceLocation(baseRl.getNamespace(), baseRl.getPath() + "." + safeName);
    }

    public String getTranslationId(String baseStr) {
        return baseStr + "." + safeName;
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
