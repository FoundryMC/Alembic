package foundry.alembic.types.potion;

import foundry.alembic.Alembic;
import foundry.alembic.types.AlembicTypeModifier;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;


public class AlembicPotionRegistry {
    public static final Map<ResourceLocation, AlembicPotionDataHolder> POTION_DATA = new HashMap<>();
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "alembic");
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.Keys.POTIONS, Alembic.MODID);

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
        MobEffect effect = new AlembicMobEffect(data);
        Attribute attribute = DamageTypeRegistry.getDamageType(data.getDamageType()).getAttribute();
        if(attribute == null){
            Alembic.LOGGER.error("Could not find attribute for damage type: " + data.getDamageType());
            return;
        }
        effect.addAttributeModifier(attribute, data.getUUID().toString(), data.getValue(), AttributeModifier.Operation.valueOf(data.getModifier()));
        MOB_EFFECTS.register(regId, () -> effect);
        POTIONS.register(regId, () -> new AlembicModifiablePotion(data));
    }
}
