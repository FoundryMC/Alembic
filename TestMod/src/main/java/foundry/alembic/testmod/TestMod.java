package foundry.alembic.testmod;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("testmod")
public class TestMod {

    public TestMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addDatagen);
    }

    private void addDatagen(final GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), new TestDamageProvider());
    }
}
