package foundry.alembic.types;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.attribute.AttributeHolder;
import foundry.alembic.attribute.AttributeRegistry;
import foundry.alembic.attribute.AttributeSet;
import foundry.alembic.util.CodecUtil;
import foundry.alembic.types.tag.AlembicTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;


public class AlembicDamageType {
    public static final Codec<AlembicDamageType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("priority").forGetter(AlembicDamageType::getPriority),
                    Codec.either(AttributeRegistry.SET_LOOKUP_CODEC, AttributeHolder.CODEC).comapFlatMap(
                            either -> {
                                if (either.left().isPresent()) {
                                    if (!either.left().get().isFullSet()) {
                                        return DataResult.error("Attribute set " + AttributeRegistry.ID_TO_SET_BIMAP.inverse().get(either.left().get()) + " must be a full set");
                                    }
                                }
                                return DataResult.success(either);
                            },
                            Function.identity()
                    ).fieldOf("attributes").forGetter(damageType -> damageType.attributesEither),
                    Codec.BOOL.fieldOf("particles").forGetter(AlembicDamageType::hasParticles),
                    CodecUtil.COLOR_CODEC.fieldOf("color").forGetter(AlembicDamageType::getColor),
                    AlembicTag.DISPATCH_CODEC.listOf().fieldOf("tags").forGetter(AlembicDamageType::getTags),
                    Codec.BOOL.fieldOf("enchant_reduction").forGetter(AlembicDamageType::hasEnchantReduction),
                    Codec.STRING.fieldOf("enchant_source").forGetter(AlembicDamageType::getEnchantSource)
            ).apply(instance, AlembicDamageType::new)
    );

    private int priority;
    private ResourceLocation id;
    private final Either<AttributeSet, AttributeHolder> attributesEither;
    private Supplier<RangedAttribute> attribute;
    private Supplier<RangedAttribute> shieldingAttribute;
    private Supplier<RangedAttribute> absorptionAttribute;
    private Supplier<RangedAttribute> resistanceAttribute;
    private boolean hasParticles;
    private DamageSource damageSource;
    private int color;
    private List<AlembicTag> tags;
    private MobEffect resistanceEffect;
    private MobEffect absorptionEffect;

    private String translationString;

    private boolean enchantReduction;
    private String enchantSource;

    public AlembicDamageType(int priority, Either<AttributeSet, AttributeHolder> attributesEither, boolean hasParticles, int color, List<AlembicTag> tags, boolean enchantReduction, String enchantSource) {
        this.priority = priority;
        this.attributesEither = attributesEither;
        this.attribute = Suppliers.memoize(() -> CodecUtil.resolveEither(attributesEither, AttributeSet::getBaseAttribute, AttributeHolder::getAttribute));
        this.shieldingAttribute = Suppliers.memoize(() -> CodecUtil.resolveEither(attributesEither, attributeSet -> attributeSet.getShieldingAttribute().get(), AttributeHolder::getShieldingAttribute));
        this.absorptionAttribute = Suppliers.memoize(() -> CodecUtil.resolveEither(attributesEither, attributeSet -> attributeSet.getAbsorptionAttribute().get(), AttributeHolder::getAbsorptionAttribute));
        this.resistanceAttribute = Suppliers.memoize(() -> CodecUtil.resolveEither(attributesEither, attributeSet -> attributeSet.getResistanceAttribute().get(), AttributeHolder::getResistanceAttribute));
        this.hasParticles = hasParticles;
        this.color = color;
        this.tags = tags;
        this.enchantReduction = enchantReduction;
        this.enchantSource = enchantSource;
    }

    public void setResistanceAttribute(RangedAttribute attribute){
        this.resistanceAttribute = Suppliers.memoize(() -> attribute);
    }

    public RangedAttribute getAttribute() {
        return attribute.get();
    }

    public RangedAttribute getShieldingAttribute() {
        return shieldingAttribute.get();
    }

    public RangedAttribute getAbsorptionAttribute() {
        return absorptionAttribute.get();
    }

    public RangedAttribute getResistanceAttribute() {
        return resistanceAttribute.get();
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

    public DamageSource getDamageSource() {
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

    public boolean hasParticles() {
        return hasParticles;
    }

    void handlePostParse(ResourceLocation id) {
        this.id = id;
        this.damageSource = new DamageSource(id.toString());
        this.translationString = id.getNamespace() + ".damage." + id.getPath();
        tags.forEach(alembicTag -> alembicTag.handlePostParse(this));
    }
}
