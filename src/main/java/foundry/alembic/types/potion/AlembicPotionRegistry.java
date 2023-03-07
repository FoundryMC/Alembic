package foundry.alembic.types.potion;

import foundry.alembic.Alembic;
import foundry.alembic.types.AlembicTypeModifier;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public class AlembicPotionRegistry {
    public static final Map<ResourceLocation, AlembicPotionDataHolder> POTION_DATA = new HashMap<>();
    public static final Map<ResourceLocation, MobEffect> MOB_EFFECT_MAP = new HashMap<>();
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "alembic");
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, "alembic");

    public static void init() {
        for(Map.Entry<ResourceLocation, AlembicPotionDataHolder> entry : POTION_DATA.entrySet()) {
            AlembicPotionDataHolder data = entry.getValue();
            data.setDamageType(entry.getKey());
            switch(data.getAttribute()) {
                case "shielding"-> {
                    String regId = AlembicTypeModifier.SHIELDING.getId(entry.getKey().getPath());
                    setupMobEffect(data, regId);
                }
                case "resistance" -> {
                    String regId = AlembicTypeModifier.RESISTANCE.getId(entry.getKey().getPath());
                    setupMobEffect(data, regId);
                }
                case "absorption" -> {
                    String regId = AlembicTypeModifier.ABSORPTION.getId(entry.getKey().getPath());
                    setupMobEffect(data, regId);
                }
                case "all" -> {
                    for(AlembicTypeModifier modfier : AlembicTypeModifier.values()){
                        String regId = modfier.getId(entry.getKey().getPath());
                        setupMobEffect(data, regId);
                    }
                }
                default -> {
                    Alembic.LOGGER.error("Could not find attribute for damage type: " + data.getDamageType());
                }
            }
        }
    }

    public static void registerPotionData(AlembicPotionDataHolder data){
        POTION_DATA.put(data.getDamageType(), data);
    }

    public static void registerPotionData(String s, AlembicPotionDataHolder data){
        POTION_DATA.put(Alembic.location(s), data);
    }

    private static void setupMobEffect(AlembicPotionDataHolder data, String regId) {
        MobEffect effect = getMobEffect(data);
        if (effect == null) return;
        MOB_EFFECT_MAP.put(Alembic.location(regId), effect);
        MOB_EFFECTS.register(regId, () -> MOB_EFFECT_MAP.get(Alembic.location(regId)));
        Potion potion = new Potion(new MobEffectInstance(effect, data.getBaseDuration(), data.getAmplifierPerLevel()));
        POTIONS.register(regId, () -> potion);
        for(int i = 0; i < data.getMaxLevel(); i++){
            Alembic.LOGGER.info("Registering potion: " + regId + "_" + i);
            Potion potion1 = new Potion(new MobEffectInstance(effect, data.getBaseDuration(), i));
            POTIONS.register(regId + "_level_" + i, () -> potion1);
        }
    }

    public static boolean doesPotionDataExist(ResourceLocation id){
        return POTION_DATA.containsKey(id);
    }

    public static void replaceWithData(ResourceLocation id, AlembicPotionDataHolder data){
        POTION_DATA.put(id, POTION_DATA.get(id).copyValues(data));
        MobEffect effect = getMobEffect(POTION_DATA.get(id));
        if (effect == null) return;
        MOB_EFFECT_MAP.put(ResourceLocation.tryParse(data.getDamageType().toString()+"_"+data.getAttribute()), effect);
        Alembic.LOGGER.info("Replacing potion: " + data.getAttribute() + " with: " + data.getDamageType());
        MobEffect eff = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(data.getDamageType().toString()+"_"+data.getAttribute()));
    }

    @Nullable
    private static MobEffect getMobEffect(AlembicPotionDataHolder data) {
        MobEffect effect = new AlembicMobEffect(MobEffectCategory.BENEFICIAL, data.getColor());
        Attribute attribute = DamageTypeRegistry.getDamageType(data.getDamageType()).getAttribute();
        if(attribute == null){
            Alembic.LOGGER.error("Could not find attribute for damage type: " + data.getDamageType());
            return null;
        }
        effect.addAttributeModifier(attribute, data.getUUID().toString(), data.getValue(), AttributeModifier.Operation.valueOf(data.getModifier()));
        return effect;
    }
}
