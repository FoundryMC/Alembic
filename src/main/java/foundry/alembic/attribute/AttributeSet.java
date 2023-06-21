package foundry.alembic.attribute;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.AlembicTypeModifier;
import foundry.alembic.potion.AlembicPotionDataHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class AttributeSet {
    public static final Codec<AttributeSet> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("base_attribute_default").forGetter(attributeSet -> attributeSet.base),
                    Codec.DOUBLE.fieldOf("base_attribute_min").forGetter(attributeSet -> attributeSet.min),
                    Codec.DOUBLE.fieldOf("base_attribute_max").forGetter(attributeSet -> attributeSet.max),
                    Codec.BOOL.optionalFieldOf("shielding", true).forGetter(attributeSet -> attributeSet.hasShielding),
                    Codec.BOOL.optionalFieldOf("absorption", true).forGetter(attributeSet -> attributeSet.hasAbsorption),
                    Codec.BOOL.optionalFieldOf("resistance", true).forGetter(attributeSet -> attributeSet.hasResistance),
                    AlembicPotionDataHolder.CODEC.optionalFieldOf("resistance_potion", AlembicPotionDataHolder.EMPTY).forGetter(attributeSet -> attributeSet.potionDataHolder)
            ).apply(instance, AttributeSet::new)
    );

    private final double base;
    private final double min;
    private final double max;
    private final boolean hasShielding;
    private final boolean hasAbsorption;
    private final boolean hasResistance;
    private final AlembicPotionDataHolder potionDataHolder;
    private ResourceLocation id;

    private final Supplier<AttributeHolder> lazyAttributeHolder = Suppliers.memoize(() ->
        new AttributeHolder(
                (RangedAttribute) ForgeRegistries.ATTRIBUTES.getValue(id),
                (RangedAttribute) ForgeRegistries.ATTRIBUTES.getValue(AlembicTypeModifier.SHIELDING.getId(id)),
                (RangedAttribute) ForgeRegistries.ATTRIBUTES.getValue(AlembicTypeModifier.ABSORPTION.getId(id)),
                (RangedAttribute) ForgeRegistries.ATTRIBUTES.getValue(AlembicTypeModifier.RESISTANCE.getId(id))
        )
    );

    public AttributeSet(double base, double min, double max, boolean hasShielding, boolean hasAbsorption, boolean hasResistance, AlembicPotionDataHolder potionDataHolder) {
        this.base = base;
        this.min = min;
        this.max = max;
        this.hasShielding = hasShielding;
        this.hasAbsorption = hasAbsorption;
        this.hasResistance = hasResistance;
        this.potionDataHolder = potionDataHolder;
    }

    void setId(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public RangedAttribute getBaseAttribute() {
        return lazyAttributeHolder.get().attribute();
    }

    public double getBase() {
        return base;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean hasShielding() {
        return hasShielding;
    }

    public boolean hasAbsorption() {
        return hasAbsorption;
    }

    public boolean hasResistance() {
        return hasResistance;
    }

    public Optional<RangedAttribute> getShieldingAttribute() {
        return Optional.ofNullable(lazyAttributeHolder.get().shieldingAttribute());
    }

    public Optional<RangedAttribute> getAbsorptionAttribute() {
        return Optional.ofNullable(lazyAttributeHolder.get().absorptionAttribute());
    }

    public Optional<RangedAttribute> getResistanceAttribute() {
        return Optional.ofNullable(lazyAttributeHolder.get().resistanceAttribute());
    }

    public Optional<AlembicPotionDataHolder> getPotionDataHolder() {
        return potionDataHolder == AlembicPotionDataHolder.EMPTY ? Optional.empty() : Optional.of(potionDataHolder);
    }

    public boolean isFullSet() {
        return hasShielding && hasAbsorption && hasResistance;
    }
}
