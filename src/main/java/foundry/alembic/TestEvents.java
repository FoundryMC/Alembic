package foundry.alembic;

import foundry.alembic.event.AlembicDamageEvent;
import foundry.alembic.event.AlembicFoodChangeEvent;
import foundry.alembic.event.AlembicSetupEvent;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import foundry.alembic.types.tags.AlembicGlobalTagPropertyHolder;
import foundry.alembic.types.tags.AlembicHungerTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Alembic.MODID)
public class TestEvents {
    @SubscribeEvent
    public static void alembicPre(AlembicDamageEvent.Pre event) {
    }

    @SubscribeEvent
    public static void alembicPost(AlembicDamageEvent.Post event) {
    }

    @SubscribeEvent
    static void alembicFoodDecrease(AlembicFoodChangeEvent.Decrease event) {
        applyHungerMod(event.getPlayer(), event.getFoodLevel());
    }

    @SubscribeEvent
    static void alembicFoodIncrease(AlembicFoodChangeEvent.Increase event) {
        applyHungerMod(event.getPlayer(), event.getFoodLevel());
    }

    private static void applyHungerMod(Player player, int foodLevel) {
        for(Map.Entry<AlembicDamageType, AlembicHungerTag> entry : AlembicGlobalTagPropertyHolder.getHungerBonuses().entrySet()){
            Attribute attribute = DamageTypeRegistry.getDamageType(entry.getKey().getId().getPath()).getAttribute(entry.getValue().getAttribute());
            if (attribute == null) return;
            if (player.getAttribute(attribute).getModifier(entry.getValue().getUUID()) != null)
                player.getAttribute(attribute).removeModifier(entry.getValue().getUUID());
            player.getAttribute(attribute).addTransientModifier(new AttributeModifier(entry.getValue().getUUID(), "Alembic hunger", entry.getValue().getScaleAmount()*(Math.floor((21- foodLevel)/(float)entry.getValue().getHungerTrigger())), entry.getValue().getOperation()));
        }
    }

    @SubscribeEvent
    public static void damageSetupEvent(AlembicSetupEvent event){
       event.addDamageType("fire_damage");
       event.addDamageType("arcane_damage");
       event.addDamageType("alchemical_damage");
       event.addDamageType("true_damage");
       event.addDamageType("physical_damage");

        event.addPotionEffect("fire_damage");
        event.addPotionEffect("arcane_damage");
        event.addPotionEffect("alchemical_damage");

        event.addParticle("true_damage");
        event.addParticle("physical_damage");
        event.addParticle("alchemical_damage");
        event.addParticle("alchemical_reaction");
        event.addParticle("arcane_damage");
        event.addParticle("arcane_spark");
        event.addParticle("fire_damage");
        event.addParticle("fire_flame");
        event.addParticle("frostbite");
        event.addParticle("soul_fire_flame");
        event.addParticle("wither_decay");
        event.addParticle("sculk_hit");
    }
}
