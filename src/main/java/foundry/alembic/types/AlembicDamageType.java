package foundry.alembic.types;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.attribute.AttributeSet;
import foundry.alembic.attribute.AttributeSetRegistry;
import foundry.alembic.types.tag.AlembicTag;
import foundry.alembic.codecs.CodecUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.List;
import java.util.function.Supplier;

public class AlembicDamageType {
    public static final Codec<AlembicDamageType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("priority").forGetter(AlembicDamageType::getPriority),
                    CodecUtil.COLOR_CODEC.fieldOf("color").forGetter(AlembicDamageType::getColor),
                    AlembicTag.DISPATCH_CODEC.listOf().fieldOf("tags").forGetter(AlembicDamageType::getTags),
                    Codec.BOOL.fieldOf("enchant_reduction").forGetter(AlembicDamageType::hasEnchantReduction),
                    Codec.STRING.fieldOf("enchant_source").forGetter(AlembicDamageType::getEnchantSource)
            ).apply(instance, AlembicDamageType::new)
    );
    public static final Codec<AlembicDamageType> NETWORK_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("priority").forGetter(AlembicDamageType::getPriority),
                    CodecUtil.COLOR_CODEC.fieldOf("color").forGetter(AlembicDamageType::getColor),
                    Codec.BOOL.fieldOf("enchant_reduction").forGetter(AlembicDamageType::hasEnchantReduction),
                    Codec.STRING.fieldOf("enchant_source").forGetter(AlembicDamageType::getEnchantSource)
            ).apply(instance, AlembicDamageType::new)
    );

    private final Supplier<AttributeSet> attributeSet = Suppliers.memoize(() -> AttributeSetRegistry.getValue(getId()));
    private int priority;
    @Deprecated(forRemoval = true, since = "1.0.0")
    private String damageSource;
    private int color;
    private List<AlembicTag> tags;

    private boolean enchantReduction;
    private String enchantSource;

    AlembicDamageType(int priority, int color, boolean enchantReduction, String enchantSource) {
        this.priority = priority;
        this.color = color;
        this.tags = List.of();
        this.enchantReduction = enchantReduction;
        this.enchantSource = enchantSource;
    }

    public AlembicDamageType(int priority, int color, List<AlembicTag> tags, boolean enchantReduction, String enchantSource) {
        this.priority = priority;
        this.color = color;
        this.tags = tags;
        this.enchantReduction = enchantReduction;
        this.enchantSource = enchantSource;
    }

    public RangedAttribute getAttribute() {
        return getAttributeSet().getDamageAttribute();
    }

    public RangedAttribute getShieldingAttribute() {
        return getAttributeSet().getShieldingAttribute();
    }

    public RangedAttribute getAbsorptionAttribute() {
        return getAttributeSet().getAbsorptionAttribute();
    }

    public RangedAttribute getResistanceAttribute() {
        return getAttributeSet().getResistanceAttribute();
    }

    public float getShieldingIgnore() {
        return getAttributeSet().getShieldingIgnore();
    }

    public float getAbsorptionIgnore() {
        return getAttributeSet().getAbsorptionIgnore();
    }

    public float getResistanceIgnore() {
        return getAttributeSet().getResistanceIgnore();
    }

    public boolean hasEnchantReduction() {
        return enchantReduction;
    }

    public String getEnchantSource() {
        return enchantSource;
    }

    public void setEnchantReduction(boolean enchantReduction) {
        this.enchantReduction = enchantReduction;
    }

    public void setEnchantSource(String enchantSource) {
        this.enchantSource = enchantSource;
    }

    public String createTranslationString() {
        return getId().getNamespace() + ".damage." + getId().getPath();
    }

    public void addTag(AlembicTag tag) {
        this.tags.add(tag);
    }

    public List<AlembicTag> getTags() {
        return this.tags;
    }

    public void clearTags() {
        this.tags.clear();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public ResourceLocation getId() {
        return DamageTypeManager.getId(this);
    }

    public String getDamageSource() {
        return damageSource;
    }

    private AttributeSet getAttributeSet() {
        return attributeSet.get();
    }

    public Component getVisualString() {
        MutableComponent mutableComponent = Component.literal("ID: " + getId().toString());
        mutableComponent.append(" Base: " + getAttribute().defaultValue);
        mutableComponent.append(" Min: " + getAttribute().minValue);
        mutableComponent.append(" Max: " + getAttribute().maxValue);
//        mutableComponent.append(" HasShielding: " + getShieldingAttribute().isPresent());
//        mutableComponent.append(" HasAbsorption: " + getAbsorptionAttribute().isPresent());
//        mutableComponent.append(" HasResistance: " + getResistanceAttribute().isPresent());
        mutableComponent.append(" Tags: " + tagString()).withStyle(s -> s.withColor(color));
        return mutableComponent;
    }

    public String tagString() {
        StringBuilder tagString = new StringBuilder();
        for (AlembicTag tag : tags) {
            tagString.append(tag.toString()).append(", ");
        }
        return tagString.toString();
    }

    public int getColor() {
        return color;
    }

    void handleTagsPostParse() {
        tags.forEach(alembicTag -> alembicTag.handlePostParse(this));
    }

    @Override
    public String toString() {
        return getId().toString();
    }
}
