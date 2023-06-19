package foundry.alembic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import foundry.alembic.attribute.AttributeRegistry;
import foundry.alembic.client.AlembicOverlayRegistry;
import foundry.alembic.compat.TESCompat;
import foundry.alembic.networking.AlembicPacketHandler;
import foundry.alembic.particle.AlembicParticleRegistry;
import foundry.alembic.types.DamageTypeRegistry;
import foundry.alembic.potion.AlembicEffectRegistry;
import foundry.alembic.types.tag.AlembicTagRegistry;
import foundry.alembic.types.tag.condition.TagConditionRegistry;
import io.github.lukebemish.defaultresources.api.ResourceProvider;
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

@Mod(Alembic.MODID)
public class Alembic {
    public static final String MODID = "alembic";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new GsonBuilder().setLenient().setPrettyPrinting().create();

    public Alembic() {
        ResourceProvider.forceInitialization();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        AttributeRegistry.initAndRegister(modEventBus);
        AlembicParticleRegistry.initAndRegister(modEventBus);
        TagConditionRegistry.init();
        AlembicTagRegistry.init();
        ForgeConfigSpec spec = AlembicConfig.makeConfig(new ForgeConfigSpec.Builder());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, spec);
        setupConfig();
        AlembicEffectRegistry.MOB_EFFECTS.register(modEventBus);
        DamageTypeRegistry.init();
        AlembicOverlayRegistry.init();
        AlembicPacketHandler.init();
        if (ModList.get().isLoaded("tslatentitystatus")) {
            TESCompat.registerClaimant();
        }
    }

    public static ResourceLocation location(String name) {
        return new ResourceLocation(MODID, name);
    }

    public void ifDevEnv(Runnable runnable) {
        if (!FMLLoader.isProduction()) {
            runnable.run();
        }
    }

    private static void setupConfig() {
//        for (String s : AlembicConfig.damageTypes.get()) {
//            LOGGER.info("Registered Damage Type: " + s);
//            ResourceLocation id = Alembic.location(s);
//            if (s.equals("physical_damage")) {
//                AlembicDamageType damageType = new AlembicDamageType(0, id, 0,0,1,false,false,false, false, 0, new ArrayList<>(), Optional.empty());
//                damageType.setResistanceAttribute((RangedAttribute) Attributes.ARMOR);
//                DamageTypeRegistry.registerDamageType(id, damageType);
//            } else {
//                AlembicDamageType damageType = new AlembicDamageType(0, id, 0,0,1,false,false,false, false, 0, new ArrayList<>(), Optional.empty());
//                DamageTypeRegistry.registerDamageType(id, damageType);
//            }
//        }
//        for (String s : AlembicConfig.potionEffects.get()) {
//            AlembicPotionDataHolder data = new AlembicPotionDataHolder();
//            try{
//                AlembicPotionRegistry.registerPotionData(s, data);
//                LOGGER.info("Registered Potion Effect: " + s);
//            } catch (Exception e) {
//                LOGGER.error("Failed to register Potion Effect: " + s);
//            }
//        }
    }
}
