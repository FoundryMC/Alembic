package foundry.alembic.potion;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.Alembic;
import foundry.alembic.codecs.CodecUtil;
import foundry.alembic.util.TagOrElements;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Set;
import java.util.UUID;

public class AlembicPotionDataHolder {
    public static final AlembicPotionDataHolder EMPTY = new AlembicPotionDataHolder();
    public static final Codec<AlembicPotionDataHolder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("value").forGetter(AlembicPotionDataHolder::getValue),
                    CodecUtil.OPERATION_CODEC.fieldOf("operation").forGetter(AlembicPotionDataHolder::getOperation),
                    Codec.BOOL.optionalFieldOf("vanilla_override", false).forGetter(AlembicPotionDataHolder::isVanillaOverride),
                    CodecUtil.setOf(TagOrElements.lazyCodec(Registries.DAMAGE_TYPE)).optionalFieldOf("immunities", Set.of()).forGetter(dataHolder -> dataHolder.immunitiesRaw),
                    Codec.INT.optionalFieldOf("max_level", 0).forGetter(AlembicPotionDataHolder::getMaxStrengthLevel),
                    Codec.INT.optionalFieldOf("base_duration", 0).forGetter(AlembicPotionDataHolder::getBaseDuration),
                    Codec.INT.optionalFieldOf("amplification_per_level", 0).forGetter(AlembicPotionDataHolder::getAmplifierPerLevel),
                    Codec.INT.optionalFieldOf("max_amplifier", 0).forGetter(AlembicPotionDataHolder::getMaxAmplifier),
                    CodecUtil.COLOR_CODEC.optionalFieldOf("color", 0).forGetter(AlembicPotionDataHolder::getColor),
                    CodecUtil.JSON_CODEC.optionalFieldOf("recipe", JsonNull.INSTANCE).forGetter(alembicPotionDataHolder -> alembicPotionDataHolder.rawRecipe)
            ).apply(instance, AlembicPotionDataHolder::new)
    );

    private final AttributeModifier.Operation operation;
    private final float value;
    private final boolean vanillaOverride;
    private final Set<TagOrElements.Lazy<DamageType>> immunitiesRaw;
    private Set<DamageType> immunities;
    private final int maxLevel;
    private final int baseDuration;
    private final int amplifierPerLevel;
    private final int maxAmplifier;
    private final int color;
    private final JsonElement rawRecipe;
    private AlembicPotionRecipe recipe;

    private UUID uuid;

    public AlembicPotionDataHolder() {
        this.value = 0;
        this.operation = AttributeModifier.Operation.ADDITION;
        this.vanillaOverride = false;
        this.immunitiesRaw = Set.of();
        this.maxLevel = 0;
        this.baseDuration = 0;
        this.amplifierPerLevel = 0;
        this.maxAmplifier = 0;
        this.color = 0;
        this.rawRecipe = JsonNull.INSTANCE;
        uuid = UUID.randomUUID();
    }

    public AlembicPotionDataHolder(float value, AttributeModifier.Operation operation, boolean vanillaOverride, Set<TagOrElements.Lazy<DamageType>> immunities, int maxLevel, int baseDuration, int amplifierPerLevel, int maxAmplifier, int color, JsonElement rawRecipe) {
        this.value = value;
        this.operation = operation;
        this.vanillaOverride = vanillaOverride;
        this.immunitiesRaw = immunities;
        this.maxLevel = maxLevel;
        this.baseDuration = baseDuration;
        this.amplifierPerLevel = amplifierPerLevel;
        this.maxAmplifier = maxAmplifier;
        this.color = color;
        this.rawRecipe = rawRecipe;
        uuid = UUID.randomUUID();
    }

    public AttributeModifier.Operation getOperation(){
        return operation;
    }

    public UUID getUUID(){
        return uuid;
    }

    public float getValue(){
        return value;
    }

    public boolean isVanillaOverride(){
        return vanillaOverride;
    }

    public Set<TagOrElements.Lazy<DamageType>> getImmunities() {
        return immunitiesRaw;
    }

    public int getMaxStrengthLevel() {
        return maxLevel;
    }

    public int getBaseDuration() {
        return baseDuration;
    }

    public int getAmplifierPerLevel() {
        return amplifierPerLevel;
    }

    public int getMaxAmplifier() {
        return maxAmplifier;
    }

    public int getColor() {
        return color;
    }

    public AlembicPotionRecipe getRecipe() {
        if (recipe == null) {
            if (!rawRecipe.isJsonNull()) {
                recipe = AlembicPotionRecipe.CODEC.parse(JsonOps.INSTANCE, rawRecipe).getOrThrow(false, Alembic.LOGGER::error);
            } else {
                recipe = AlembicPotionRecipe.EMPTY;
            }
        }
        return recipe;
    }
}
