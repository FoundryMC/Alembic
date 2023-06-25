package foundry.alembic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import foundry.alembic.attribute.AttributeRegistry;
import foundry.alembic.attribute.UUIDManager;
import foundry.alembic.compat.TESCompat;
import foundry.alembic.networking.AlembicPacketHandler;
import foundry.alembic.particle.AlembicParticleRegistry;
import foundry.alembic.types.DamageTypeRegistry;
import foundry.alembic.potion.AlembicMobEffectRegistry;
import foundry.alembic.types.tag.AlembicTagRegistry;
import foundry.alembic.types.tag.condition.TagConditionRegistry;
import io.github.lukebemish.defaultresources.api.ResourceProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;

import java.io.IOException;

@Mod(Alembic.MODID)
public class Alembic {
    public static final String MODID = "alembic";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new GsonBuilder().setLenient().setPrettyPrinting().create();

    public Alembic() {
        ResourceProvider.forceInitialization();
        try {
            UUIDManager.loadCache();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        AttributeRegistry.initAndRegister(modEventBus);
        AlembicParticleRegistry.initAndRegister(modEventBus);
        TagConditionRegistry.init();
        AlembicTagRegistry.init();

        ForgeConfigSpec spec = AlembicConfig.makeConfig(new ForgeConfigSpec.Builder());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, spec);

        AlembicMobEffectRegistry.MOB_EFFECTS.register(modEventBus);
        DamageTypeRegistry.init();
        AlembicPacketHandler.init();

        MinecraftForge.EVENT_BUS.addListener(this::saveCache);

        if (FMLLoader.getDist().isClient()) {
            AlembicClient.init(modEventBus);
        }

        if (ModList.get().isLoaded("tslatentitystatus")) {
            TESCompat.registerClaimant();
        }
    }

    public static ResourceLocation location(String name) {
        return new ResourceLocation(MODID, name);
    }

    public static void ifPrintDebug(Runnable runnable) {
        if (AlembicConfig.enableDebugPrints.get()) {
            runnable.run();
        }
    }

    public static boolean isDebugEnabled() {
        return AlembicConfig.enableDebugPrints.get();
    }

    private void saveCache(ServerStoppingEvent event) {
        try {
            UUIDManager.saveCache();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
