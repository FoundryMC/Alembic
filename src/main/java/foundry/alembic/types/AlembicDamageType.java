package foundry.alembic.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.CodecUtil;
import foundry.alembic.types.potion.AlembicPotionDataHolder;
import foundry.alembic.types.tags.AlembicTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;


public class AlembicDamageType {
    public static final Codec<AlembicDamageType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("priority").forGetter(AlembicDamageType::getPriority),
                    Codec.DOUBLE.fieldOf("default").forGetter(AlembicDamageType::getBase),
                    Codec.DOUBLE.fieldOf("min").forGetter(AlembicDamageType::getMin),
                    Codec.DOUBLE.fieldOf("max").forGetter(AlembicDamageType::getMax),
                    Codec.BOOL.fieldOf("shielding").forGetter(AlembicDamageType::hasShielding),
                    Codec.BOOL.fieldOf("resistance").forGetter(AlembicDamageType::hasResistance),
                    Codec.BOOL.fieldOf("absorption").forGetter(AlembicDamageType::hasAbsorption),
                    Codec.BOOL.fieldOf("particles").forGetter(AlembicDamageType::hasParticles),
                    CodecUtil.COLOR_CODEC.fieldOf("color").forGetter(AlembicDamageType::getColor),
                    AlembicTag.DISPATCH_CODEC.listOf().fieldOf("tags").forGetter(AlembicDamageType::getTags),
                    AlembicPotionDataHolder.CODEC.optionalFieldOf("potion").forGetter(AlembicDamageType::getPotionDataHolderOptional),
                    Codec.BOOL.fieldOf("enchant_reduction").forGetter(AlembicDamageType::hasEnchantReduction),
                    Codec.STRING.fieldOf("enchant_source").forGetter(AlembicDamageType::getEnchantSource)
            ).apply(instance, AlembicDamageType::new)
    );

    private int priority;
    private ResourceLocation id;
    private double base;
    private double min;
    private double max;
    private boolean hasShielding;
    private boolean hasResistance;
    private boolean hasAbsorption;
    private boolean hasParticles;
    private AlembicAttribute attribute;
    private DamageSource damageSource;
    private int color;
    private List<AlembicTag> tags;
    private RangedAttribute shieldAttribute;
    private RangedAttribute resistanceAttribute;
    private MobEffect resistanceEffect;
    private AlembicAttribute absorptionAttribute;
    private MobEffect absorptionEffect;

    private String translationString;

    private Optional<AlembicPotionDataHolder> potionDataHolder;

    private boolean enchantReduction;
    private String enchantSource;

    public AlembicDamageType(int priority, ResourceLocation id, double base, double min, double max, boolean hasShielding, boolean hasResistance, boolean hasAbsorption, boolean hasParticles, int color, List<AlembicTag> tags, Optional<AlembicPotionDataHolder> potionDataHolder) {
        this.priority = priority;
        this.id = id;
        this.base = base;
        this.min = min;
        this.max = max;
        this.hasShielding = hasShielding;
        this.hasResistance = hasResistance;
        this.hasAbsorption = hasAbsorption;
        this.attribute = new AlembicAttribute(id.toString(), base, min, max);
        this.damageSource = new DamageSource(id.toString());
        this.color = color;
        this.hasParticles = hasParticles;
        this.shieldAttribute = new AlembicAttribute(id + "_shielding", 0, 0, 1024);
        this.resistanceAttribute = new AlembicAttribute(id + "_resistance", 1, -1024, 1024);
        this.absorptionAttribute = new AlembicAttribute(id + "_absorption", 0, 0, 1024);
        this.translationString = "alembic.damage." + id.getNamespace() + "." + id.getPath();
        this.tags = tags;
        this.potionDataHolder = potionDataHolder;
    }

    private AlembicDamageType(int priority, double base, double min, double max, boolean hasShielding, boolean hasResistance, boolean hasAbsorption, boolean hasParticles, int color, List<AlembicTag> tags, @Nullable Optional<AlembicPotionDataHolder> potionDataHolder, boolean enchantReduction, String enchantSource) {
        this.priority = priority;
        this.base = base;
        this.min = min;
        this.max = max;
        this.hasShielding = hasShielding;
        this.hasResistance = hasResistance;
        this.hasAbsorption = hasAbsorption;
        this.hasParticles = hasParticles;
        this.color = color;
        this.tags = tags;
        this.potionDataHolder = potionDataHolder;
        this.enchantReduction = enchantReduction;
        this.enchantSource = enchantSource;
    }

    private void setupPotionData(){
        potionDataHolder.ifPresent(dataHolder -> dataHolder.setDamageType(getId()));
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

    public void setPotionDataHolder(Optional<AlembicPotionDataHolder> potionDataHolder) {
        this.potionDataHolder = potionDataHolder;
    }

    public AlembicPotionDataHolder getPotionDataHolder() {
        return potionDataHolder.orElse(null);
    }

    public Optional<AlembicPotionDataHolder> getPotionDataHolderOptional() {
        return potionDataHolder;
    }

    public String getTranslationString() {
        return translationString;
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
        return id;
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

    public boolean hasResistance() {
        return hasResistance;
    }

    public boolean hasAbsorption() {
        return hasAbsorption;
    }

    public AlembicAttribute getAttribute() {
        return attribute;
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }

    public Component getVisualString() {
        return Component.literal("ID: " + id.toString() +
                        " Base: " + base + " Min: " + min +
                        " Max: " + max + " Shielding: " + hasShielding +
                        " Resistance: " + hasResistance +
                        " Absorption: " + hasAbsorption +
                        " Tags: " + tagString())
                .withStyle(s -> s.withColor(color));
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

    public boolean hasParticles() {
        return hasParticles;
    }

    void handlePostParse(ResourceLocation id) {
        this.id = id;
        this.attribute = new AlembicAttribute(id.toString(), base, min, max);
        this.damageSource = new DamageSource(id.toString());
        this.shieldAttribute = new AlembicAttribute(id + "_shielding", 0, 0, 1024);
        this.resistanceAttribute = new AlembicAttribute(id + "_resistance", 1, -1024, 1024);
        this.absorptionAttribute = new AlembicAttribute(id + "_absorption", 0, 0, 1024);
        this.translationString = "alembic.damage." + id.getNamespace() + "." + id.getPath();
        tags.forEach(alembicTag -> alembicTag.handlePostParse(this));
        setupPotionData();
    }

    public RangedAttribute getShieldAttribute() {
        return shieldAttribute;
    }

    public RangedAttribute getResistanceAttribute() {
        return resistanceAttribute;
    }

    public AlembicAttribute getAbsorptionAttribute() {
        return absorptionAttribute;
    }

    public void setShieldAttribute(RangedAttribute shieldAttribute) {
        this.shieldAttribute = shieldAttribute;
    }

    public void setResistanceAttribute(RangedAttribute resistanceAttribute) {
        this.resistanceAttribute = resistanceAttribute;
    }

    public void setAbsorptionAttribute(AlembicAttribute absorptionAttribute) {
        this.absorptionAttribute = absorptionAttribute;
    }

    AlembicDamageType copyValues(AlembicDamageType copyFrom) {
        this.priority = copyFrom.priority;
        this.base = copyFrom.base;
        this.min = copyFrom.min;
        this.max = copyFrom.max;
        this.attribute.setBaseValue(base);
        this.attribute.setMinValue(min);
        this.attribute.setMaxValue(max);
        this.hasShielding = copyFrom.hasShielding;
        this.hasResistance = copyFrom.hasResistance;
        this.hasAbsorption = copyFrom.hasAbsorption;
        this.color = copyFrom.color;
        this.hasParticles = copyFrom.hasParticles;
        this.tags = copyFrom.tags;
        this.potionDataHolder = copyFrom.potionDataHolder;
        this.enchantSource = copyFrom.enchantSource;
        this.enchantReduction = copyFrom.enchantReduction;
        return this;
    }

    public RangedAttribute getAttribute(String type){
        return switch (type) {
            case "shielding" -> shieldAttribute;
            case "resistance" -> resistanceAttribute;
            case "absorption" -> absorptionAttribute;
            default -> attribute;
        };
    }
}
