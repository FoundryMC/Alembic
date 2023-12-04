package foundry.alembic.attribute;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import foundry.alembic.ForgeEvents;
import foundry.alembic.mobeffect.AlembicMobEffect;
import foundry.alembic.mobeffect.mobeffects.ImmunityMobEffect;
import foundry.alembic.potion.AlembicPotionDataHolder;
import foundry.alembic.resources.ResourceProviderHelper;
import foundry.alembic.types.AlembicAttribute;
import foundry.alembic.types.AlembicTypeModifier;
import foundry.alembic.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AttributeSetRegistry {
    private static final BiMap<ResourceLocation, AttributeSet> ID_TO_SET_BIMAP = HashBiMap.create();

    private static final Map<String, DeferredRegister<Attribute>> ATTRIBUTE_REGISTRY_MAP = new HashMap<>();
    private static final Map<String, DeferredRegister<Potion>> POTION_REGISTRY_MAP = new HashMap<>();
    private static final Map<String, DeferredRegister<MobEffect>> MOB_EFFECT_REGISTRY_MAP = new HashMap<>();

    public static Collection<AttributeSet> getValues() {
        return Collections.unmodifiableCollection(ID_TO_SET_BIMAP.values());
    }

    @Nullable
    public static AttributeSet getValue(ResourceLocation id) {
        return ID_TO_SET_BIMAP.get(id);
    }

    public static ResourceLocation getId(AttributeSet attributeSet) {
        return ID_TO_SET_BIMAP.inverse().get(attributeSet);
    }

    public static boolean exists(ResourceLocation id) {
        return ID_TO_SET_BIMAP.containsKey(id);
    }

    private static DeferredRegister<Attribute> getAttributeRegister(String resourceId) {
        return ATTRIBUTE_REGISTRY_MAP.computeIfAbsent(resourceId, s -> DeferredRegister.create(ForgeRegistries.Keys.ATTRIBUTES, s));
    }

    private static DeferredRegister<Potion> getPotionRegister(String resourceId) {
        return POTION_REGISTRY_MAP.computeIfAbsent(resourceId, s -> DeferredRegister.create(ForgeRegistries.Keys.POTIONS, s));
    }

    private static DeferredRegister<MobEffect> getMobEffectRegister(String resourceId) {
        return MOB_EFFECT_REGISTRY_MAP.computeIfAbsent(resourceId, s -> DeferredRegister.create(ForgeRegistries.Keys.MOB_EFFECTS, s));
    }

    public static void initAndRegister(IEventBus modBus) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : ResourceProviderHelper.readAsJson("attribute_sets", jsonElement -> CraftingHelper.processConditions(jsonElement.getAsJsonObject(), "forge:conditions", ICondition.IContext.TAGS_INVALID)).entrySet()) {
            DataResult<AttributeSet> setResult = AttributeSet.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if (setResult.error().isPresent()) {
                throw new IllegalStateException("Error loading {" + entry.getKey() + "}: " + setResult.error().get().message());
            }
            AttributeSet set = setResult.result().get();
            ResourceLocation id = Utils.sanitize(entry.getKey(), "attribute_sets/", ".json"); // TODO: replace with FileToIdConverter

            if (ID_TO_SET_BIMAP.containsKey(id)) {
                Alembic.LOGGER.error("Attribute set already present " + id);
                continue;
            }

            ID_TO_SET_BIMAP.put(id, set);

            DeferredRegister<Attribute> deferredRegister = getAttributeRegister(id.getNamespace());
            set.getDamageData().ifPresent(data -> {
                deferredRegister.register(id.getPath(), () -> createFromData(id.toLanguageKey("attribute"), data));
            });
            set.getShieldingData().ifPresent(data -> {
                String regId = AlembicTypeModifier.SHIELDING.getTranslationId(id.getPath());
                String descId = AlembicTypeModifier.SHIELDING.getTranslationId(id.toLanguageKey("attribute"));
                deferredRegister.register(regId, () -> createFromData(descId, data));
            });
            set.getAbsorptionData().ifPresent(data -> {
                String regId = AlembicTypeModifier.ABSORPTION.getTranslationId(id.getPath());
                String descId = AlembicTypeModifier.ABSORPTION.getTranslationId(id.toLanguageKey("attribute"));
                deferredRegister.register(regId, () -> createFromData(descId, data));
            });
            set.getResistanceData().ifPresent(data -> {
                String regId = AlembicTypeModifier.RESISTANCE.getTranslationId(id.getPath());
                String descId = AlembicTypeModifier.RESISTANCE.getTranslationId(id.toLanguageKey("attribute"));
                deferredRegister.register(regId, () -> createFromData(descId, data));
            });

            set.getPotionDataHolder().ifPresent(alembicPotionDataHolder -> {
                registerEffectsAndPotions(id, set, alembicPotionDataHolder);
            });
        }

        for (DeferredRegister<Attribute> register : ATTRIBUTE_REGISTRY_MAP.values()) {
            register.register(modBus);
        }
        for (DeferredRegister<MobEffect> register : MOB_EFFECT_REGISTRY_MAP.values()) {
            register.register(modBus);
        }
        for (DeferredRegister<Potion> register : POTION_REGISTRY_MAP.values()) {
            register.register(modBus);
        }
    }

    private static AlembicAttribute createFromData(String descId, RangedAttributeData data) {
        return new AlembicAttribute(descId, data.base(), data.min(), data.max());
    }

    private static void registerEffectsAndPotions(ResourceLocation setId, AttributeSet attributeSet, AlembicPotionDataHolder dataHolder) {
        DeferredRegister<MobEffect> mobEffectRegister = getMobEffectRegister(setId.getNamespace());
        DeferredRegister<Potion> potionRegister = getPotionRegister(setId.getNamespace());

        if (setId.getPath().contains("fire_damage")) {
            Supplier<MobEffect> effectObj;
            if (!dataHolder.getImmunities().isEmpty()) {
                effectObj = mobEffectRegister.register("fire_resistance", () -> new ImmunityMobEffect(MobEffectCategory.BENEFICIAL, dataHolder.getColor(), dataHolder.getImmunities())
                        .addAttributeModifier(attributeSet.getResistanceAttribute(), ForgeEvents.ALEMBIC_FIRE_RESIST_UUID.toString(), 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));
            } else {
                effectObj = () -> MobEffects.FIRE_RESISTANCE;
            }
            potionRegister.register("strong_fire_resistance", () -> new Potion(new MobEffectInstance(effectObj.get(), 1200, 1)));
        } else {
            String resistanceId = setId.getPath() + "_resistance";

            RegistryObject<MobEffect> effectObj = mobEffectRegister.register(resistanceId, () -> createMobEffect(attributeSet, dataHolder));
            potionRegister.register(resistanceId, () -> new Potion(new MobEffectInstance(effectObj.get(), 3600, 0)));
            potionRegister.register("long_" + resistanceId, () -> new Potion(new MobEffectInstance(effectObj.get(), 9600, 0)));
            if (dataHolder.getMaxStrengthLevel() > 1) {
                potionRegister.register("strong_" + resistanceId, () -> new Potion(new MobEffectInstance(effectObj.get(), 3600, 1)));
                int lastAmplifier = 1;
                for (int i = 2; i < dataHolder.getMaxStrengthLevel(); i++) {
                    final int level = i + 1;
                    final int amplifier = lastAmplifier + dataHolder.getAmplifierPerLevel();
                    potionRegister.register("strong_" + resistanceId + "_" + i, () -> new Potion(new MobEffectInstance(effectObj.get(), dataHolder.getBaseDuration() * level, Math.max(dataHolder.getMaxAmplifier(), amplifier)))); // TODO: is this right?
                    lastAmplifier = amplifier;
                }
            }
        }
    }

    private static MobEffect createMobEffect(AttributeSet attributeSet, AlembicPotionDataHolder dataHolder) {
        if (!dataHolder.getImmunities().isEmpty()) {
            return new ImmunityMobEffect(MobEffectCategory.BENEFICIAL, dataHolder.getColor(), dataHolder.getImmunities())
                    .addAttributeModifier(attributeSet.getResistanceAttribute(), dataHolder.getUUID().toString(), dataHolder.getValue(), dataHolder.getOperation());
        }
        return new AlembicMobEffect(attributeSet.getResistanceAttribute(), dataHolder);
    }
}
