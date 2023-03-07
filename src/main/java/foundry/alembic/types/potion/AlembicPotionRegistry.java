package foundry.alembic.types.potion;

import foundry.alembic.Alembic;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.AlembicTypeModfier;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

import static foundry.alembic.types.DamageTypeRegistry.DAMAGE_TYPES;


public class AlembicPotionRegistry {
    public static final Map<ResourceLocation, AlembicPotionDataHolder> POTION_DATA = new HashMap<>();
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "alembic");

    public static void init() {
        for(Map.Entry<ResourceLocation, AlembicPotionDataHolder> entry : POTION_DATA.entrySet()) {
            AlembicPotionDataHolder data = entry.getValue();
            data.setDamageType(entry.getKey());
            switch(data.getAttribute()) {
                case "shielding"-> {
                    String regId = AlembicTypeModfier.SHIELDING.getId(entry.getKey().getPath());
                    setupMobEffect(data, regId);
                }
                case "resistance" -> {
                    String regId = AlembicTypeModfier.RESISTANCE.getId(entry.getKey().getPath());
                    setupMobEffect(data, regId);
                }
                case "absorption" -> {
                    String regId = AlembicTypeModfier.ABSORPTION.getId(entry.getKey().getPath());
                    setupMobEffect(data, regId);
                }
                case "all" -> {
                    for(AlembicTypeModfier modfier : AlembicTypeModfier.values()){
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
        MobEffect effect = new AlembicMobEffect(MobEffectCategory.BENEFICIAL, data.getColor());
        Attribute attribute = DamageTypeRegistry.getDamageType(data.getDamageType()).getAttribute();
        if(attribute == null){
            Alembic.LOGGER.error("Could not find attribute for damage type: " + data.getDamageType());
            return;
        }
        effect.addAttributeModifier(attribute, data.getUUID().toString(), data.getValue(), AttributeModifier.Operation.valueOf(data.getModifier()));
        MOB_EFFECTS.register(regId, () -> effect);
    }
}
