package foundry.alembic;

import foundry.alembic.event.AlembicDamageEvent;
import foundry.alembic.event.AlembicFoodDecreaseEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Alembic.MODID)
public class TestEvents {
    @SubscribeEvent
    public static void alembicPre(AlembicDamageEvent.Pre event){
    }

    @SubscribeEvent
    public static void alembicPost(AlembicDamageEvent.Post event){
    }

    @SubscribeEvent
    static void alembicFoodDecrease(AlembicFoodDecreaseEvent event){
    }
}
