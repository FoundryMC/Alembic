package foundry.alembic.potion;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.util.CodecUtil;
import foundry.alembic.damagesource.DamageSourceIdentifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.*;

public class AlembicPotionDataHolder {
    public static final AlembicPotionDataHolder EMPTY = new AlembicPotionDataHolder();
    public static final Codec<AlembicPotionDataHolder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("value").forGetter(AlembicPotionDataHolder::getValue),
                    CodecUtil.OPERATION_CODEC.fieldOf("operation").forGetter(AlembicPotionDataHolder::getOperation),
                    Codec.BOOL.optionalFieldOf("vanilla_override", false).forGetter(AlembicPotionDataHolder::isVanillaOverride),
                    CodecUtil.setOf(DamageSourceIdentifier.EITHER_CODEC).optionalFieldOf("immunities", Set.of()).forGetter(dataHolder -> dataHolder.immunitiesEither),
                    Codec.INT.optionalFieldOf("max_level", 0).forGetter(AlembicPotionDataHolder::getMaxLevel),
                    Codec.INT.optionalFieldOf("base_duration", 0).forGetter(AlembicPotionDataHolder::getBaseDuration),
                    Codec.INT.optionalFieldOf("amplification_per_level", 0).forGetter(AlembicPotionDataHolder::getAmplifierPerLevel),
                    Codec.INT.optionalFieldOf("max_amplifier", 0).forGetter(AlembicPotionDataHolder::getMaxAmplifier),
                    CodecUtil.COLOR_CODEC.optionalFieldOf("color", 0).forGetter(AlembicPotionDataHolder::getColor),
                    AlembicPotionRecipe.CODEC.optionalFieldOf("recipe", AlembicPotionRecipe.EMPTY).forGetter(AlembicPotionDataHolder::getRecipe)
            ).apply(instance, AlembicPotionDataHolder::new)
    );

    private AttributeModifier.Operation operation;
    private float value;
    private boolean vanillaOverride;
    private final Set<Either<DamageSourceIdentifier.DefaultWrappedSource, DamageSourceIdentifier>> immunitiesEither;
    private Set<DamageSourceIdentifier> immunities;
    private int maxLevel;

    private int baseDuration;
    private int amplifierPerLevel;
    private int maxAmplifier;
    private int color;
    private AlembicPotionRecipe recipe;

    private UUID uuid;

    public AlembicPotionDataHolder(){
        this.value = 0;
        this.operation = AttributeModifier.Operation.ADDITION;
        this.vanillaOverride = false;
        this.immunitiesEither = Set.of();
        this.immunities = Set.of();
        this.maxLevel = 0;
        this.baseDuration = 0;
        this.amplifierPerLevel = 0;
        this.maxAmplifier = 0;
        this.color = 0;
        this.recipe = AlembicPotionRecipe.EMPTY;
        uuid = UUID.randomUUID();
    }

    public AlembicPotionDataHolder(float value, AttributeModifier.Operation operation, boolean vanillaOverride, Set<Either<DamageSourceIdentifier.DefaultWrappedSource, DamageSourceIdentifier>> immunities, int maxLevel, int baseDuration, int amplifierPerLevel, int maxAmplifier, int color, AlembicPotionRecipe recipe) {
        this.value = value;
        this.operation = operation;
        this.vanillaOverride = vanillaOverride;
        this.immunitiesEither = immunities;
        ImmutableSet.Builder<DamageSourceIdentifier> setBuilder = ImmutableSet.builder();
        for (Either<DamageSourceIdentifier.DefaultWrappedSource, DamageSourceIdentifier> either : immunities) {
            if (either.left().isPresent()) {
                DamageSourceIdentifier.DefaultWrappedSource wrappedSource = either.left().get();
                for (DamageSourceIdentifier identifier : wrappedSource.getIdentifiers()) {
                    setBuilder.add(identifier);
                }
            } else {
                setBuilder.add(either.right().get());
            }
        }
        this.immunities = setBuilder.build();
        this.maxLevel = maxLevel;
        this.baseDuration = baseDuration;
        this.amplifierPerLevel = amplifierPerLevel;
        this.maxAmplifier = maxAmplifier;
        this.color = color;
        this.recipe = recipe;
        uuid = UUID.randomUUID();
    }

    public void setOperation(AttributeModifier.Operation operation){
        this.operation = operation;
    }

    public AttributeModifier.Operation getOperation(){
        return operation;
    }

    public UUID getUUID(){
        return uuid;
    }

    public void setValue(float value){
        this.value = value;
    }

    public void setVanillaOverride(boolean vanillaOverride){
        this.vanillaOverride = vanillaOverride;
    }

    public void setImmunities(Set<DamageSourceIdentifier> immunities){
        this.immunities = immunities;
    }

    public void setMaxLevel(int maxLevel){
        this.maxLevel = maxLevel;
    }

    public void setBaseDuration(int baseDuration){
        this.baseDuration = baseDuration;
    }


    public void setAmplifierPerLevel(int amplifierPerLevel){
        this.amplifierPerLevel = amplifierPerLevel;
    }

    public void setMaxAmplifier(int maxAmplifier){
        this.maxAmplifier = maxAmplifier;
    }

    public void setColor(int color){
        this.color = color;
    }

    public void setRecipe(AlembicPotionRecipe recipe){
        this.recipe = recipe;
    }

    public float getValue(){
        return value;
    }

    public boolean isVanillaOverride(){
        return vanillaOverride;
    }

    public Set<DamageSourceIdentifier> getImmunities(){
        return immunities;
    }

    public int getMaxLevel(){
        return maxLevel;
    }


    public int getBaseDuration(){
        return baseDuration;
    }


    public int getAmplifierPerLevel(){
        return amplifierPerLevel;
    }

    public int getMaxAmplifier(){
        return maxAmplifier;
    }

    public int getColor() {
        return color;
    }

    public AlembicPotionRecipe getRecipe() {
        return recipe;
    }

    AlembicPotionDataHolder copyValues(AlembicPotionDataHolder from) {
        this.value = from.value;
        this.operation = from.operation;
        this.vanillaOverride = from.vanillaOverride;
        this.immunities = from.immunities;
        this.maxLevel = from.maxLevel;
        this.baseDuration = from.baseDuration;
        this.amplifierPerLevel = from.amplifierPerLevel;
        this.maxAmplifier = from.maxAmplifier;
        this.color = from.color;
        this.recipe = from.recipe;
        return this;
    }
}
