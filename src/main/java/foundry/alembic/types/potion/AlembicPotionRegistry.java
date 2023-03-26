package foundry.alembic.types.potion;

import foundry.alembic.Alembic;
import foundry.alembic.caps.AlembicFlammableHandler;
import foundry.alembic.mixin.MobEffectAccessor;
import foundry.alembic.types.AlembicTypeModifier;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class AlembicPotionRegistry {
    public static final Map<ResourceLocation, AlembicPotionDataHolder> POTION_DATA = new HashMap<>();
    public static final Map<ResourceLocation, MobEffect> MOB_EFFECT_MAP = new HashMap<>();

    public static final Map<ResourceLocation, AlembicPotionDataHolder> IMMUNITY_DATA = new HashMap<>();
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "alembic");
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, "alembic");

    public static final RegistryObject<MobEffect> FIRE = MOB_EFFECTS.register("fire", FireMobEffect::new);
    public static final RegistryObject<MobEffect> FROSTBITE = MOB_EFFECTS.register("frostbite", FrostbiteMobEffect::new);
    public static final RegistryObject<MobEffect> SOUL_FIRE = MOB_EFFECTS.register("soul_fire", SoulFireMobEffect::new);


    public static void init() {
        for(Map.Entry<ResourceLocation, AlembicPotionDataHolder> entry : POTION_DATA.entrySet()) {
            AlembicPotionDataHolder data = entry.getValue();
            data.setDamageType(entry.getKey());
            switch(data.getAttribute()) {
                case "resistance", "all" -> {
                    String regId = AlembicTypeModifier.RESISTANCE.getId(entry.getKey().getPath());
                    setupMobEffect(data, regId);
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
        if(regId.contains("fire_damage_resistance")){
            Potion potion_long = new Potion(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 1));
            POTIONS.register("fire_resistance_strong", () -> potion_long);
            return;
        } else if (data.getDamageType().getPath().contains("fire_damage")){
            return;
        }
        regId = Alembic.location(regId).toString();
        MobEffect effect = getMobEffect(data);
        if (effect == null) return;
        MOB_EFFECT_MAP.put(ResourceLocation.tryParse(regId), effect);
        IMMUNITY_DATA.put(ResourceLocation.tryParse(regId), data);
        regId = regId.substring(regId.indexOf(":") + 1);
        if(regId.contains("fire_resistance")) return;
        String finalRegId = regId;
        MOB_EFFECTS.register(regId, () -> MOB_EFFECT_MAP.get(Alembic.location(finalRegId)));
        Potion potion = new Potion(new MobEffectInstance(effect, 3600, 0));
        Potion potion_long = new Potion(new MobEffectInstance(effect, 9600, 0));
        Potion potion_strong = new Potion(new MobEffectInstance(effect, 1200, 1));
        POTIONS.register(regId, () -> potion);
        POTIONS.register(regId + "_long", () -> potion_long);
        POTIONS.register(regId + "_strong", () -> potion_strong);
        for(int i = 0; i < data.getMaxLevel(); i++){
            Alembic.LOGGER.info("Registering potion: " + regId + "_" + i);
            Potion potion1 = new Potion(new MobEffectInstance(effect, 3600*(i+1), i));
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
        if(eff == null) return;
        if(eff.getAttributeModifiers().entrySet().stream().anyMatch(entry -> entry.getValue().getId().equals(data.getUUID()))){
            eff.getAttributeModifiers().entrySet().stream().filter(entry -> entry.getValue().getId().equals(data.getUUID())).forEach(entry -> {
                eff.getAttributeModifiers().remove(entry.getKey(), entry.getValue());
            });
        }
        Attribute at = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.tryParse(data.getDamageType().toString()+"_"+data.getAttribute()));
        if (at == null) return;
        eff.addAttributeModifier(at, data.getUUID().toString(), data.getValue(), AttributeModifier.Operation.valueOf(data.getModifier()));
        ((MobEffectAccessor)eff).setColor(data.getColor());
    }

    @Nullable
    private static MobEffect getMobEffect(AlembicPotionDataHolder data) {
        MobEffect effect = new AlembicMobEffect(data);
        Attribute attribute = DamageTypeRegistry.getDamageType(data.getDamageType()).getAttribute();
        if(attribute == null){
            Alembic.LOGGER.error("Could not find attribute for damage type: " + data.getDamageType());
            return null;
        }
        effect.addAttributeModifier(attribute, data.getUUID().toString(), data.getValue(), AttributeModifier.Operation.valueOf(data.getModifier()));
        return effect;
    }

    public static class SoulFireMobEffect extends AlembicMobEffect {
        public SoulFireMobEffect() {
            super(MobEffectCategory.HARMFUL, 0x9760FB);
        }

        @Override
        public void onApplication(@Nullable MobEffectInstance effectInstance, @Nullable Entity source, LivingEntity entity, int amplifier) {
            if(effectInstance== null) return;
            entity.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent(cap -> cap.setFireType("soul"));
            if(!entity.isOnFire()){
                entity.setRemainingFireTicks(effectInstance.getDuration());
            } else if (entity.getRemainingFireTicks() < effectInstance.getDuration()) {
                entity.setRemainingFireTicks(effectInstance.getDuration());
            }
        }
        @Override
        public boolean shouldTickEffect(@Nullable MobEffectInstance effectInstance, @Nullable LivingEntity entity, int ticksRemaining, int amplifier) {
            return true;
        }

        @Override
        public void tick(LivingEntity entity, @Nullable MobEffectInstance effectInstance, int amplifier) {
            if(effectInstance== null) return;
            if(!entity.isOnFire()){
                entity.setRemainingFireTicks(effectInstance.getDuration());
            } else if (entity.getRemainingFireTicks() < effectInstance.getDuration()) {
                entity.setRemainingFireTicks(effectInstance.getDuration());
            }
        }
    }

    public static class FrostbiteMobEffect extends AlembicMobEffect {
        public FrostbiteMobEffect() {
            super(MobEffectCategory.HARMFUL, 0x00F1F1);
        }

        @Override
        public void onApplication(@Nullable MobEffectInstance effectInstance, @Nullable Entity source, LivingEntity entity, int amplifier) {
            if(effectInstance== null) return;
            if(entity.getTicksFrozen() < effectInstance.getDuration()){
                entity.setTicksFrozen(effectInstance.getDuration());
            }
        }
        @Override
        public boolean shouldTickEffect(@Nullable MobEffectInstance effectInstance, @Nullable LivingEntity entity, int ticksRemaining, int amplifier) {
            return true;
        }

        @Override
        public void tick(LivingEntity entity, @Nullable MobEffectInstance effectInstance, int amplifier) {
            if(effectInstance== null) return;
            if(entity.getTicksFrozen() < effectInstance.getDuration()){
                entity.setTicksFrozen(effectInstance.getDuration());
            }
        }
    }

    public static class FireMobEffect extends AlembicMobEffect {
        public FireMobEffect() {
            super(MobEffectCategory.HARMFUL, 0xF14700);
        }

        @Override
        public void onApplication(@Nullable MobEffectInstance effectInstance, @Nullable Entity source, LivingEntity entity, int amplifier) {
            if(effectInstance== null) return;
            entity.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent(cap -> cap.setFireType("normal"));
            if(!entity.isOnFire()){
                entity.setRemainingFireTicks(effectInstance.getDuration());
            } else if (entity.getRemainingFireTicks() < effectInstance.getDuration()) {
                entity.setRemainingFireTicks(effectInstance.getDuration());
            }
        }

        @Override
        public boolean shouldTickEffect(@Nullable MobEffectInstance effectInstance, @Nullable LivingEntity entity, int ticksRemaining, int amplifier) {
            return true;
        }

        @Override
        public void tick(LivingEntity entity, @Nullable MobEffectInstance effectInstance, int amplifier) {
            if(effectInstance== null) return;
            if(!entity.isOnFire()){
                entity.setRemainingFireTicks(effectInstance.getDuration());
            } else if (entity.getRemainingFireTicks() < effectInstance.getDuration()) {
                entity.setRemainingFireTicks(effectInstance.getDuration());
            }
        }
    }
}
