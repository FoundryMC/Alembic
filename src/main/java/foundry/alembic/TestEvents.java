package foundry.alembic;

import foundry.alembic.event.AlembicDamageEvent;
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
}
