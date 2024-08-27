package foundry.alembic;

import com.mojang.logging.LogUtils;
import foundry.alembic.attribute.AttributeSetRegistry;
import foundry.alembic.compat.Compat;
import foundry.alembic.compat.TESCompat;
import foundry.alembic.mobeffect.AlembicMobEffectRegistry;
import foundry.alembic.networking.AlembicPacketHandler;
import foundry.alembic.particle.AlembicParticleRegistry;
import foundry.alembic.types.tag.AlembicTagRegistry;
import foundry.alembic.types.tag.condition.TagConditionRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(Alembic.MODID)
public class Alembic {
    public static final String MODID = "alembic";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean DUMP_STATIC_REGISTRIES = System.getProperty("alembic.dump_static_registries") != null;

    public Alembic() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        AlembicMobEffectRegistry.MOB_EFFECTS.register(modEventBus);

        AttributeSetRegistry.initAndRegister(modEventBus);
        AlembicParticleRegistry.initAndRegister(modEventBus);

        TagConditionRegistry.init();
        AlembicTagRegistry.init();

        ForgeConfigSpec spec = AlembicConfig.makeConfig(new ForgeConfigSpec.Builder());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, spec);

        AlembicPacketHandler.init();

        if (FMLLoader.getDist().isClient()) {
            AlembicClient.init(modEventBus);
        }

        Compat.init();
    }

    public static ResourceLocation location(String name) {
        return new ResourceLocation(MODID, name);
    }

    public static void printInDebug(Supplier<String> stringSupplier) {
        if (AlembicConfig.enableDebugPrints.get()) {
            Alembic.LOGGER.debug(stringSupplier.get());
        }
    }

    public static boolean isDebugEnabled() {
        return AlembicConfig.enableDebugPrints.get();
    }
}
