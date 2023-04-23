package foundry.alembic.types.potion;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.Alembic;
import foundry.alembic.util.CodecUtil;
import foundry.alembic.damagesource.DamageSourceIdentifier;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class AlembicPotionDataHolder {
    public static final Codec<AlembicPotionDataHolder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("value").forGetter(AlembicPotionDataHolder::getValue),
                    CodecUtil.OPERATION_CODEC.fieldOf("operation").forGetter(AlembicPotionDataHolder::getOperation),
                    Codec.BOOL.optionalFieldOf("vanilla_override").forGetter(AlembicPotionDataHolder::getVanillaOverrideOptional),
                    CodecUtil.setOf(DamageSourceIdentifier.EITHER_CODEC).optionalFieldOf("immunities").forGetter(dataHolder -> dataHolder.immunitiesEither),
                    Codec.INT.optionalFieldOf("max_level").forGetter(AlembicPotionDataHolder::getMaxLevelOptional),
                    Codec.INT.optionalFieldOf("base_duration").forGetter(AlembicPotionDataHolder::getBaseDurationOptional),
                    Codec.INT.optionalFieldOf("amplification_per_level").forGetter(AlembicPotionDataHolder::getAmplifierPerLevelOptional),
                    Codec.INT.optionalFieldOf("max_amplifier").forGetter(AlembicPotionDataHolder::getMaxAmplifierOptional),
                    CodecUtil.COLOR_CODEC.optionalFieldOf("color").forGetter(AlembicPotionDataHolder::getColorOptional),
                    ItemStack.CODEC.optionalFieldOf("reagent").forGetter(AlembicPotionDataHolder::getReagentOptional)
            ).apply(instance, AlembicPotionDataHolder::new)
    );

    private AttributeModifier.Operation operation;
    private float value;
    private Optional<Boolean> vanillaOverride;
    private final Optional<Set<Either<DamageSourceIdentifier.DefaultWrappedSource, DamageSourceIdentifier>>> immunitiesEither;
    private Set<DamageSourceIdentifier> immunities;
    private Optional<Integer> maxLevel;

    private Optional<Integer> baseDuration;
    private Optional<Integer> amplifierPerLevel;
    private Optional<Integer> maxAmplifier;
    private Optional<Integer> color;
    private Optional<ItemStack> reagent;

    private UUID uuid;

    public AlembicPotionDataHolder(){
        this.value = 0;
        this.operation = AttributeModifier.Operation.ADDITION;
        this.vanillaOverride = Optional.empty();
        this.immunitiesEither = Optional.empty();
        this.immunities = Set.of();
        this.maxLevel = Optional.empty();
        this.baseDuration = Optional.empty();
        this.amplifierPerLevel = Optional.empty();
        this.maxAmplifier = Optional.empty();
        this.color = Optional.empty();
        this.reagent = Optional.empty();
        uuid = UUID.randomUUID();
    }

    public AlembicPotionDataHolder(float value, AttributeModifier.Operation operation, Optional<Boolean> vanillaOverride, Optional<Set<Either<DamageSourceIdentifier.DefaultWrappedSource, DamageSourceIdentifier>>> immunities, Optional<Integer> maxLevel, Optional<Integer> baseDuration, Optional<Integer> amplifierPerLevel, Optional<Integer> maxAmplifier, Optional<Integer> color, Optional<ItemStack> reagent){
        this.value = value;
        this.operation = operation;
        this.vanillaOverride = vanillaOverride;
        this.immunitiesEither = immunities;
        ImmutableSet.Builder<DamageSourceIdentifier> setBuilder = ImmutableSet.builder();
        for (Either<DamageSourceIdentifier.DefaultWrappedSource, DamageSourceIdentifier> either : immunities.orElse(Set.of())) {
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
        this.reagent = reagent;
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

    public void setVanillaOverride(Optional<Boolean> vanillaOverride){
        this.vanillaOverride = vanillaOverride;
    }

    public void setImmunities(Set<DamageSourceIdentifier> immunities){
        this.immunities = immunities;
    }

    public void setMaxLevel(Optional<Integer> maxLevel){
        this.maxLevel = maxLevel;
    }

    public void setBaseDuration(Optional<Integer> baseDuration){
        this.baseDuration = baseDuration;
    }


    public void setAmplifierPerLevel(Optional<Integer> amplifierPerLevel){
        this.amplifierPerLevel = amplifierPerLevel;
    }

    public void setMaxAmplifier(Optional<Integer> maxAmplifier){
        this.maxAmplifier = maxAmplifier;
    }

    public void setColor(Optional<Integer> color){
        this.color = color;
    }

    public void setReagent(Optional<ItemStack> reagent){
        this.reagent = reagent;
    }

    public float getValue(){
        return value;
    }

    public boolean getVanillaOverride(){
        return vanillaOverride.orElse(false);
    }

    public Optional<Boolean> getVanillaOverrideOptional(){
        return vanillaOverride;
    }

    public Set<DamageSourceIdentifier> getImmunities(){
        return immunities;
    }

    public int getMaxLevel(){
        return maxLevel.orElse(0);
    }

    public Optional<Integer> getMaxLevelOptional(){
        return maxLevel;
    }

    public int getBaseDuration(){
        return baseDuration.orElse(0);
    }

    public Optional<Integer> getBaseDurationOptional(){
        return baseDuration;
    }


    public int getAmplifierPerLevel(){
        return amplifierPerLevel.orElse(0);
    }

    public Optional<Integer> getAmplifierPerLevelOptional(){
        return amplifierPerLevel;
    }

    public int getMaxAmplifier(){
        return maxAmplifier.orElse(0);
    }

    public Optional<Integer> getMaxAmplifierOptional(){
        return maxAmplifier;
    }

    public int getColor(){
        return color.orElse(0);
    }

    public Optional<Integer> getColorOptional(){
        return color;
    }

    public ItemStack getReagent(){
        return reagent.orElse(null);
    }

    public Optional<ItemStack> getReagentOptional(){
        return reagent;
    }

    AlembicPotionDataHolder copyValues(AlembicPotionDataHolder from){
        this.value = from.value;
        this.operation = from.operation;
        this.vanillaOverride = from.vanillaOverride;
        this.immunities = from.immunities;
        this.maxLevel = from.maxLevel;
        this.baseDuration = from.baseDuration;
        this.amplifierPerLevel = from.amplifierPerLevel;
        this.maxAmplifier = from.maxAmplifier;
        this.color = from.color;
        this.reagent = from.reagent;
        return this;
    }
}
