package foundry.alembic;

import foundry.alembic.event.AlembicDamageEvent;
import foundry.alembic.event.AlembicFoodChangeEvent;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import foundry.alembic.types.tag.tags.AlembicGlobalTagPropertyHolder;
import foundry.alembic.types.tag.tags.AlembicHungerTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

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
            Attribute attribute = entry.getValue().getTypeModifier().getAffectedAttribute(entry.getKey());
            if (attribute == null) return;
            if (player.getAttribute(attribute).getModifier(entry.getValue().getUUID()) != null)
                player.getAttribute(attribute).removeModifier(entry.getValue().getUUID());
            player.getAttribute(attribute).addTransientModifier(new AttributeModifier(entry.getValue().getUUID(), "Alembic hunger", entry.getValue().getScaleAmount()*(Math.floor((21- foodLevel)/(float)entry.getValue().getHungerTrigger())), entry.getValue().getOperation()));
        }
    }
}
