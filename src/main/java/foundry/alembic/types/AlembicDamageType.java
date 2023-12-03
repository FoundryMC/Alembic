package foundry.alembic.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.attribute.AttributeSetRegistry;
import foundry.alembic.attribute.AttributeSet;
import foundry.alembic.util.CodecUtil;
import foundry.alembic.types.tag.AlembicTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.List;

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

    private int priority;
    private ResourceLocation id;
    private AttributeSet attributeSet;
    private String damageSource;
    private int color;
    private List<AlembicTag> tags;
    private MobEffect resistanceEffect;
    private MobEffect absorptionEffect;

    private boolean enchantReduction;
    private String enchantSource;

    public AlembicDamageType(int priority, int color, List<AlembicTag> tags, boolean enchantReduction, String enchantSource) {
        this.priority = priority;
        this.color = color;
        this.tags = tags;
        this.enchantReduction = enchantReduction;
        this.enchantSource = enchantSource;
    }

    public RangedAttribute getAttribute() {
        return attributeSet.getDamageAttribute();
    }

    public RangedAttribute getShieldingAttribute() {
        return attributeSet.getShieldingAttribute();
    }

    public RangedAttribute getAbsorptionAttribute() {
        return attributeSet.getAbsorptionAttribute();
    }

    public RangedAttribute getResistanceAttribute() {
        return attributeSet.getResistanceAttribute();
    }

    public float getShieldingIgnore() {
        return attributeSet.getShieldingIgnore();
    }

    public float getAbsorptionIgnore() {
        return attributeSet.getAbsorptionIgnore();
    }

    public float getResistanceIgnore() {
        return attributeSet.getResistanceIgnore();
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
        return id.getNamespace() + ".damage." + id.getPath();
    }

    public void addTag(AlembicTag tag) {
        this.tags.add(tag);
    }

    public List<AlembicTag> getTags() {
        return this.tags;
    }

//    public void runTags() {
//
//    }

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
        return id;
    }

    public String getDamageSource() {
        return damageSource;
    }

    public Component getVisualString() {
        MutableComponent mutableComponent = Component.literal("ID: " + id.toString());
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

    void handlePostParse(ResourceLocation id) {
        this.id = id;
        this.damageSource = id.toString();
        this.attributeSet = AttributeSetRegistry.getValue(id);
        tags.forEach(alembicTag -> alembicTag.handlePostParse(this));
    }
}
