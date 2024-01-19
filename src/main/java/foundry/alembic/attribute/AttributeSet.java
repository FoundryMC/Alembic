package foundry.alembic.attribute;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.potion.AlembicPotionDataHolder;
import foundry.alembic.types.AlembicTypeModifier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

public class AttributeSet {
    public static final Codec<Either<RangedAttributeData, Holder<Attribute>>> DATA_OR_REGISTERED = Codec.either(RangedAttributeData.CODEC, BuiltInRegistries.ATTRIBUTE.holderByNameCodec());
    public static final Codec<AttributeSet> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    DATA_OR_REGISTERED.fieldOf("damage_attribute").forGetter(attributeSet -> attributeSet.damage),
                    DATA_OR_REGISTERED.optionalFieldOf("shielding_attribute", Either.left(RangedAttributeData.DEFAULT_SHIELDING)).forGetter(attributeSet -> attributeSet.shielding),
                    DATA_OR_REGISTERED.optionalFieldOf("absorption_attribute", Either.left(RangedAttributeData.DEFAULT_ABSORPTION)).forGetter(attributeSet -> attributeSet.absorption),
                    DATA_OR_REGISTERED.optionalFieldOf("resistance_attribute", Either.left(RangedAttributeData.DEFAULT_RESISTANCE)).forGetter(attributeSet -> attributeSet.resistance),
                    AlembicPotionDataHolder.CODEC.optionalFieldOf("resistance_potion", AlembicPotionDataHolder.EMPTY).forGetter(attributeSet -> attributeSet.potionDataHolder),
                    Codec.FLOAT.optionalFieldOf("shielding_ignore", 0.0f).forGetter(attributeSet -> attributeSet.shieldingIgnore),
                    Codec.FLOAT.optionalFieldOf("absorption_ignore", 0.0f).forGetter(attributeSet -> attributeSet.absorptionIgnore),
                    Codec.FLOAT.optionalFieldOf("resistance_ignore", 0.0f).forGetter(attributeSet -> attributeSet.resistanceIgnore)
            ).apply(instance, AttributeSet::new)
    );

    private final Either<RangedAttributeData, Holder<Attribute>> damage;
    private final Either<RangedAttributeData, Holder<Attribute>> shielding;
    private final Either<RangedAttributeData, Holder<Attribute>> absorption;
    private final Either<RangedAttributeData, Holder<Attribute>> resistance;
    private final AlembicPotionDataHolder potionDataHolder;
    private final float shieldingIgnore;
    private final float absorptionIgnore;
    private final float resistanceIgnore;

    public AttributeSet(Either<RangedAttributeData, Holder<Attribute>> damage,
                        Either<RangedAttributeData, Holder<Attribute>> shielding,
                        Either<RangedAttributeData, Holder<Attribute>> absorption,
                        Either<RangedAttributeData, Holder<Attribute>> resistance,
                        AlembicPotionDataHolder potionDataHolder, float shieldingIgnore,
                        float absorptionIgnore, float resistanceIgnore) {
        this.damage = damage;
        this.shielding = shielding;
        this.absorption = absorption;
        this.resistance = resistance;
        this.potionDataHolder = potionDataHolder;
        this.shieldingIgnore = 1.0f - shieldingIgnore;
        this.absorptionIgnore = 1.0f - absorptionIgnore;
        this.resistanceIgnore = 1.0f - resistanceIgnore;
    }

    Optional<RangedAttributeData> getDamageData() {
        return damage.left();
    }

    Optional<RangedAttributeData> getShieldingData() {
        return shielding.left();
    }

    Optional<RangedAttributeData> getAbsorptionData() {
        return absorption.left();
    }

    Optional<RangedAttributeData> getResistanceData() {
        return resistance.left();
    }

    public float getShieldingIgnore() {
        return shieldingIgnore;
    }

    public float getAbsorptionIgnore() {
        return absorptionIgnore;
    }

    public float getResistanceIgnore() {
        return resistanceIgnore;
    }

    @ApiStatus.Internal
    public Optional<AlembicPotionDataHolder> getPotionDataHolder() {
        return potionDataHolder == AlembicPotionDataHolder.EMPTY ? Optional.empty() : Optional.of(potionDataHolder);
    }

    public RangedAttribute getDamageAttribute() {
        return (RangedAttribute) damage.map(data -> BuiltInRegistries.ATTRIBUTE.get(getId()), Holder::value);
    }

    public RangedAttribute getShieldingAttribute() {
        return (RangedAttribute) shielding.map(data -> BuiltInRegistries.ATTRIBUTE.get(AlembicTypeModifier.SHIELDING.computeAttributeId(getId())), Holder::value);
    }

    public RangedAttribute getAbsorptionAttribute() {
        return (RangedAttribute) absorption.map(data -> BuiltInRegistries.ATTRIBUTE.get(AlembicTypeModifier.ABSORPTION.computeAttributeId(getId())), Holder::value);
    }

    public RangedAttribute getResistanceAttribute() {
        return (RangedAttribute) resistance.map(data -> BuiltInRegistries.ATTRIBUTE.get(AlembicTypeModifier.RESISTANCE.computeAttributeId(getId())), Holder::value);
    }

    private ResourceLocation getId() {
        return AttributeSetRegistry.getId(this);
    }
}
