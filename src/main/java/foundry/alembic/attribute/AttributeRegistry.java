package foundry.alembic.attribute;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import foundry.alembic.mobeffects.ImmunityMobEffect;
import foundry.alembic.resources.ResourceProviderHandler;
import foundry.alembic.types.AlembicAttribute;
import foundry.alembic.types.AlembicTypeModifier;
import foundry.alembic.types.potion.AlembicMobEffect;
import foundry.alembic.types.potion.AlembicPotionDataHolder;
import foundry.alembic.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AttributeRegistry {
    private static final Map<String, DeferredRegister<Attribute>> ATTRIBUTE_REGISTRY_MAP = new HashMap<>();
    private static final Map<String, DeferredRegister<Potion>> POTION_REGISTRY_MAP = new HashMap<>();
    private static final Map<String, DeferredRegister<MobEffect>> MOB_EFFECT_REGISTRY_MAP = new HashMap<>();
    public static final BiMap<ResourceLocation, AttributeSet> ID_TO_SET_BIMAP = HashBiMap.create();

    public static final Codec<AttributeSet> SET_LOOKUP_CODEC = ResourceLocation.CODEC.comapFlatMap(
            resourceLocation -> {
                if (!ID_TO_SET_BIMAP.containsKey(resourceLocation)) {
                    return DataResult.error("Attribute set: " + resourceLocation + " does not exist!");
                }
                return DataResult.success(ID_TO_SET_BIMAP.get(resourceLocation));
            },
            ID_TO_SET_BIMAP.inverse()::get
    );

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
        for (Map.Entry<ResourceLocation, JsonElement> entry : ResourceProviderHandler.readAsJson("alembic_pack/attribute_sets").entrySet()) {
            DataResult<AttributeSet> setResult = AttributeSet.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if (setResult.error().isPresent()) {
                throw new IllegalStateException(setResult.error().get().message());
            }
            AttributeSet set = setResult.result().get();
            ResourceLocation sanitizedId = Utils.sanitize(entry.getKey(), "alembic_pack/attribute_sets/", ".json");
            set.setId(sanitizedId);

            if (ID_TO_SET_BIMAP.containsKey(sanitizedId)) {
                Alembic.LOGGER.error("Attribute set already present " + sanitizedId);
                continue;
            }

            ID_TO_SET_BIMAP.put(sanitizedId, set);

            DeferredRegister<Attribute> deferredRegister = getAttributeRegister(sanitizedId.getNamespace());
            deferredRegister.register(sanitizedId.getPath(), () -> new AlembicAttribute(sanitizedId.toLanguageKey("attribute"), set.getBase(), set.getMin(), set.getMax()));
            if (set.hasShielding()) register(deferredRegister, sanitizedId, AlembicTypeModifier.SHIELDING, 0, 0, 1024);
            if (set.hasAbsorption()) register(deferredRegister, sanitizedId, AlembicTypeModifier.ABSORPTION, 0, 0, 1024);
            if (set.hasResistance()) {
                register(deferredRegister, sanitizedId, AlembicTypeModifier.RESISTANCE, 1, -1024, 1024);
                if (set.getPotionDataHolder().isPresent()) {
                    registerEffectsAndPotions(sanitizedId, set, set.getPotionDataHolder().get());
                }
            }
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

    private static void register(DeferredRegister<Attribute> register, ResourceLocation id, AlembicTypeModifier modifier, double base, double min, double max) {
        register.register(modifier.getTranslationId(id.getPath()), () -> new AlembicAttribute(modifier.getTranslationId(id.toLanguageKey("attribute")), base, min, max));
    }

    private static void registerEffectsAndPotions(ResourceLocation setId, AttributeSet attributeSet, AlembicPotionDataHolder dataHolder) {
        DeferredRegister<MobEffect> mobEffectRegister = getMobEffectRegister(setId.getNamespace());
        DeferredRegister<Potion> potionRegister = getPotionRegister(setId.getNamespace());

        if (setId.getPath().contains("fire_damage")) {
            Supplier<MobEffect> effectObj;
            if (!dataHolder.getImmunities().isEmpty()) {
                effectObj = mobEffectRegister.register("fire_resistance", () -> new ImmunityMobEffect(MobEffectCategory.BENEFICIAL, dataHolder.getColor(), dataHolder.getImmunities()));
            } else {
                effectObj = () -> MobEffects.FIRE_RESISTANCE;
            }
            potionRegister.register("fire_resistance_strong", () -> new Potion(new MobEffectInstance(effectObj.get(), 1200, 1)));
            return;
        }

        String resistanceId = setId.getPath() + "_resistance";

        RegistryObject<MobEffect> effectObj = mobEffectRegister.register(resistanceId, () -> createMobEffect(attributeSet, dataHolder));
        potionRegister.register(resistanceId, () -> new Potion(new MobEffectInstance(effectObj.get(), 3600, 0)));
        potionRegister.register(resistanceId + "_long", () -> new Potion(new MobEffectInstance(effectObj.get(), 9600, 0)));
        potionRegister.register(resistanceId + "_strong", () -> new Potion(new MobEffectInstance(effectObj.get(), 1200, 1)));
        for(int i = 0; i < dataHolder.getMaxLevel(); i++) {
            final int real = i;
            potionRegister.register(resistanceId + "_level_" + i, () -> new Potion(new MobEffectInstance(effectObj.get(), 3600*(real+1), real))); // TODO: is this right?
        }
    }

    private static MobEffect createMobEffect(AttributeSet attributeSet, AlembicPotionDataHolder dataHolder) {
        if (!dataHolder.getImmunities().isEmpty()) {
            return new ImmunityMobEffect(MobEffectCategory.BENEFICIAL, dataHolder.getColor(), dataHolder.getImmunities())
                    .addAttributeModifier(attributeSet.getResistanceAttribute().get(), dataHolder.getUUID().toString(), dataHolder.getValue(), dataHolder.getOperation());
        }
        return new AlembicMobEffect(attributeSet.getResistanceAttribute().get(), dataHolder);
    }
}