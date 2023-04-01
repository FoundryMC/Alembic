package foundry.alembic.attribute;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.AlembicAttribute;
import foundry.alembic.types.AlembicTypeModifier;
import foundry.alembic.types.potion.AlembicPotionDataHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
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
                    AlembicPotionDataHolder.CODEC.optionalFieldOf("resistance_potion").forGetter(attributeSet -> Optional.ofNullable(attributeSet.potionDataHolder))
            ).apply(instance, AttributeSet::new)
    );

    private final double base;
    private final double min;
    private final double max;
    private final boolean hasShielding;
    private final boolean hasAbsorption;
    private final boolean hasResistance;
    @Nullable
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

    public AttributeSet(double base, double min, double max, boolean hasShielding, boolean hasAbsorption, boolean hasResistance, Optional<AlembicPotionDataHolder> potionDataHolder) {
        this.base = base;
        this.min = min;
        this.max = max;
        this.hasShielding = hasShielding;
        this.hasAbsorption = hasAbsorption;
        this.hasResistance = hasResistance;
        this.potionDataHolder = potionDataHolder.orElse(null);
    }

    void setId(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public RangedAttribute getBaseAttribute() {
        return lazyAttributeHolder.get().getAttribute();
    }

    public RangedAttribute getShieldingAttribute() {
        return lazyAttributeHolder.get().getShieldingAttribute();
    }

    public RangedAttribute getAbsorptionAttribute() {
        return lazyAttributeHolder.get().getAbsorptionAttribute();
    }

    public RangedAttribute getResistanceAttribute() {
        return lazyAttributeHolder.get().getResistanceAttribute();
    }

    public Optional<AlembicPotionDataHolder> getPotionDataHolder() {
        return Optional.ofNullable(potionDataHolder);
    }

    public boolean isFullSet() {
        return hasShielding && hasAbsorption && hasResistance;
    }

    void registerAttributes(DeferredRegister<Attribute> deferredRegister) {
        deferredRegister.register(id.getPath(), () -> new AlembicAttribute(id.toLanguageKey("attribute"), base, min, max));
        if (hasShielding) register(deferredRegister, AlembicTypeModifier.SHIELDING, 0, 0, 1024);
        if (hasAbsorption) register(deferredRegister, AlembicTypeModifier.ABSORPTION, 0, 0, 1024);
        if (hasResistance) register(deferredRegister, AlembicTypeModifier.RESISTANCE, 1, -1024, 1024);
    }

    private void register(DeferredRegister<Attribute> register, AlembicTypeModifier modifier, double base, double min, double max) {
        register.register(modifier.getTranslationId(id.toString()), () -> new AlembicAttribute(modifier.getTranslationId(id.toLanguageKey("attribute")), base, min, max));
    }
}
