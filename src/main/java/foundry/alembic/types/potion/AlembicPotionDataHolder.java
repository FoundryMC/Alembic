package foundry.alembic.types.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.Alembic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AlembicPotionDataHolder {
    public static final List<AlembicPotionDataHolder> POTION_DATA_HOLDERS = new ArrayList<>();
    public static final Codec<AlembicPotionDataHolder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("attribute").forGetter(AlembicPotionDataHolder::getAttribute),
                    Codec.FLOAT.fieldOf("value").forGetter(AlembicPotionDataHolder::getValue),
                    Codec.STRING.fieldOf("operation").forGetter(AlembicPotionDataHolder::getModifier),
                    Codec.BOOL.optionalFieldOf("vanilla_override").forGetter(AlembicPotionDataHolder::getVanillaOverrideOptional),
                    Codec.list(Codec.STRING).optionalFieldOf("immunities").forGetter(AlembicPotionDataHolder::getImmunitiesOptional),
                    Codec.INT.optionalFieldOf("max_level").forGetter(AlembicPotionDataHolder::getMaxLevelOptional),
                    Codec.INT.optionalFieldOf("base_duration").forGetter(AlembicPotionDataHolder::getBaseDurationOptional),
                    Codec.INT.optionalFieldOf("amplification_per_level").forGetter(AlembicPotionDataHolder::getAmplifierPerLevelOptional),
                    Codec.INT.optionalFieldOf("max_amplifier").forGetter(AlembicPotionDataHolder::getMaxAmplifierOptional),
                    Codec.STRING.comapFlatMap(AlembicPotionDataHolder::getColorDataResult, AlembicPotionDataHolder::getColorString).optionalFieldOf("color").forGetter(AlembicPotionDataHolder::getColorOptional),
                    ItemStack.CODEC.optionalFieldOf("reagent").forGetter(AlembicPotionDataHolder::getReagentOptional)
            ).apply(instance, AlembicPotionDataHolder::new)
            );

    private static DataResult<Integer> getColorDataResult(String color){
        return DataResult.success(Integer.parseInt(color.replace("#",""), 16));
    }

    private static String getColorString(Integer color){
        return String.format("#%06X", (0xFFFFFF & color));
    }
    private String attribute;
    private String modifier;
    private float value;
    private Optional<Boolean> vanillaOverride;
    private Optional<List<String>> immunities;
    private Optional<Integer> maxLevel;

    private Optional<Integer> baseDuration;
    private Optional<Integer> amplifierPerLevel;
    private Optional<Integer> maxAmplifier;
    private Optional<Integer> color;
    private Optional<ItemStack> reagent;

    private ResourceLocation damageType;

    private UUID uuid;

    public AlembicPotionDataHolder(){
        this.attribute = "all";
        this.value = 0;
        this.modifier = "ADDITION";
        this.vanillaOverride = Optional.empty();
        this.immunities = Optional.empty();
        this.maxLevel = Optional.empty();
        this.baseDuration = Optional.empty();
        this.amplifierPerLevel = Optional.empty();
        this.maxAmplifier = Optional.empty();
        this.color = Optional.empty();
        this.reagent = Optional.empty();
        this.damageType = Alembic.location("physical_damage");
        uuid = UUID.randomUUID();
        POTION_DATA_HOLDERS.add(this);
    }

    public AlembicPotionDataHolder(String attribute, float value, String modifier, Optional<Boolean> vanillaOverride, Optional<List<String>> immunities, Optional<Integer> maxLevel, Optional<Integer> baseDuration, Optional<Integer> amplifierPerLevel, Optional<Integer> maxAmplifier, Optional<Integer> color, Optional<ItemStack> reagent){
        this.attribute = attribute;
        this.value = value;
        this.modifier = modifier;
        this.vanillaOverride = vanillaOverride;
        this.immunities = immunities;
        this.maxLevel = maxLevel;
        this.baseDuration = baseDuration;
        this.amplifierPerLevel = amplifierPerLevel;
        this.maxAmplifier = maxAmplifier;
        this.color = color;
        this.reagent = reagent;
        POTION_DATA_HOLDERS.add(this);
        uuid = UUID.randomUUID();
    }

    public void setModifier(String modifier){
        this.modifier = modifier;
    }

    public String getModifier(){
        return modifier;
    }

    public UUID getUUID(){
        return uuid;
    }

    public void setDamageType(ResourceLocation damageType){
        this.damageType = damageType;
    }

    public ResourceLocation getDamageType(){
        return damageType;
    }

    public void setAttribute(String attribute){
        this.attribute = attribute;
    }

    public void setValue(float value){
        this.value = value;
    }

    public void setVanillaOverride(Optional<Boolean> vanillaOverride){
        this.vanillaOverride = vanillaOverride;
    }

    public void setImmunities(Optional<List<String>> immunities){
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

    public String getAttribute(){
        return attribute;
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

    public List<String> getImmunities(){
        return immunities.orElse(null);
    }

    public Optional<List<String>> getImmunitiesOptional(){
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
        this.attribute = from.attribute;
        this.value = from.value;
        this.modifier = from.modifier;
        this.vanillaOverride = from.vanillaOverride;
        this.immunities = from.immunities;
        this.maxLevel = from.maxLevel;
        this.baseDuration = from.baseDuration;
        this.amplifierPerLevel = from.amplifierPerLevel;
        this.maxAmplifier = from.maxAmplifier;
        this.color = from.color;
        this.reagent = from.reagent;
        this.damageType = from.damageType;
        return this;
    }
}
