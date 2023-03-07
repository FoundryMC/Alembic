package foundry.alembic.types.potion;

import foundry.alembic.Alembic;
import foundry.alembic.types.AlembicDamageType;
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
        for(Map.Entry<ResourceLocation, AlembicPotionDataHolder> entry : POTION_DATA.entrySet()){
            AlembicPotionDataHolder data = entry.getValue();
            data.setDamageType(entry.getKey());
            switch(data.getAttribute()){
                case "shielding"->{
                    String regId = entry.getKey().getPath() + "_shield";
                    setupMobEffect(data, regId);
                }
                case "resistance" -> {
                    String regId = entry.getKey().getPath() + "_resistance";
                    setupMobEffect(data, regId);
                }
                case "absorption" -> {
                    String regId = entry.getKey().getPath() + "_absorption";
                    setupMobEffect(data, regId);
                }
                case "all" -> {
                    for(String attribute : new String[]{"shielding", "resistance", "absorption"}){
                        String regId = entry.getKey().getPath() + "_"+attribute;
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

    // TODO: for some reason this isnt actually registering even though there's no error or anything at all.
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
