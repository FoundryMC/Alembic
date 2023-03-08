package foundry.alembic;

import foundry.alembic.event.AlembicDamageEvent;
import foundry.alembic.event.AlembicFoodChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
    }

    @SubscribeEvent
    static void alembicFoodIncrease(AlembicFoodChangeEvent.Increase event) {
    }
}
