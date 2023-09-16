package foundry.alembic.attribute;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.AlembicTypeModifier;
import foundry.alembic.potion.AlembicPotionDataHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.Optional;

public class AttributeSet {
    public static final Codec<Holder<Attribute>> GET_OR_CREATE_HOLDER_CODEC = ResourceLocation.CODEC.comapFlatMap(
            resourceLocation -> Registry.ATTRIBUTE.getOrCreateHolder(ResourceKey.create(Registry.ATTRIBUTE_REGISTRY, resourceLocation)),
            attributeHolder -> attributeHolder.unwrapKey().get().location()
    );
    public static final Codec<Either<RangedAttributeData, Holder<Attribute>>> DATA_OR_REGISTERED = Codec.either(RangedAttributeData.CODEC, GET_OR_CREATE_HOLDER_CODEC);
    public static final Codec<AttributeSet> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    DATA_OR_REGISTERED.fieldOf("damage_attribute").forGetter(attributeSet -> attributeSet.damage),
                    DATA_OR_REGISTERED.fieldOf("shielding_attribute").forGetter(attributeSet -> attributeSet.shielding),
                    DATA_OR_REGISTERED.fieldOf("absorption_attribute").forGetter(attributeSet -> attributeSet.absorption),
                    DATA_OR_REGISTERED.fieldOf("resistance_attribute").forGetter(attributeSet -> attributeSet.resistance),
                    AlembicPotionDataHolder.CODEC.optionalFieldOf("resistance_potion", AlembicPotionDataHolder.EMPTY).forGetter(attributeSet -> attributeSet.potionDataHolder)
            ).apply(instance, AttributeSet::new)
    );

    private final Either<RangedAttributeData, Holder<Attribute>> damage;
    private final Either<RangedAttributeData, Holder<Attribute>> shielding;
    private final Either<RangedAttributeData, Holder<Attribute>> absorption;
    private final Either<RangedAttributeData, Holder<Attribute>> resistance;
    private final AlembicPotionDataHolder potionDataHolder;

    public AttributeSet(Either<RangedAttributeData, Holder<Attribute>> damage,
                        Either<RangedAttributeData, Holder<Attribute>> shielding,
                        Either<RangedAttributeData, Holder<Attribute>> absorption,
                        Either<RangedAttributeData, Holder<Attribute>> resistance,
                        AlembicPotionDataHolder potionDataHolder) {
        this.damage = damage;
        this.shielding = shielding;
        this.absorption = absorption;
        this.resistance = resistance;
        this.potionDataHolder = potionDataHolder;
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

    Optional<AlembicPotionDataHolder> getPotionDataHolder() {
        return potionDataHolder == AlembicPotionDataHolder.EMPTY ? Optional.empty() : Optional.of(potionDataHolder);
    }

    public RangedAttribute getDamageAttribute() {
        return (RangedAttribute) damage.map(data -> Registry.ATTRIBUTE.get(getId()), Holder::value);
    }

    public RangedAttribute getShieldingAttribute() {
        return (RangedAttribute) damage.map(data -> Registry.ATTRIBUTE.get(AlembicTypeModifier.SHIELDING.computeId(getId())), Holder::value);
    }

    public RangedAttribute getAbsorptionAttribute() {
        return (RangedAttribute) damage.map(data -> Registry.ATTRIBUTE.get(AlembicTypeModifier.ABSORPTION.computeId(getId())), Holder::value);
    }

    public RangedAttribute getResistanceAttribute() {
        return (RangedAttribute) damage.map(data -> Registry.ATTRIBUTE.get(AlembicTypeModifier.RESISTANCE.computeId(getId())), Holder::value);
    }

    private ResourceLocation getId() {
        return AttributeSetRegistry.getId(this);
    }
}
