package foundry.alembic.testmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("testmod")
public class TestMod {

    public TestMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addDatagen);
        MinecraftForge.EVENT_BUS.addListener(this::rightClick);
    }

    private void addDatagen(final GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), new TestDamageProvider());
    }

    private void rightClick(PlayerInteractEvent.RightClickItem event){
        if(event.getEntity().level.isClientSide) return;
    }
}
