package foundry.alembic;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.mojang.logging.LogUtils;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import foundry.alembic.types.tags.AlembicTagRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Alembic.MODID)
public class Alembic {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "alembic";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public Alembic() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        AlembicTagRegistry.init();
        setupDamageTypes();
        ForgeConfigSpec spec = AlembicConfig.makeConfig(new ForgeConfigSpec.Builder());

        final CommentedFileConfig file = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve("alembic-common.toml"))
                .sync().autosave().writingMode(WritingMode.REPLACE).build();
        file.load();
        spec.setConfig(file);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, spec);
        setupConfig();
        MinecraftForge.EVENT_BUS.register(this);
        DamageTypeRegistry.DAMAGE_ATTRIBUTES.register(modEventBus);
        DamageTypeRegistry.DEFENSIVE_ATTRIBUTES.register(modEventBus);
        DamageTypeRegistry.init();
    }

    public static ResourceLocation location(String name) {
        return new ResourceLocation(MODID, name);
    }
    public static void setupConfig(){
        for (String s : AlembicConfig.list.get()) {
            LOGGER.info("Registered Damage Type: " + s);
            AlembicDamageType damageType = new AlembicDamageType(0, Alembic.location(s), 0,0,1,false,false,false, 0, false);
            DamageTypeRegistry.registerDamageType(damageType);
        }
    }

    public static void setupDamageTypes(){
        AlembicAPI.addDefaultDamageType("fire_damage");
        AlembicAPI.addDefaultDamageType("eldritch_damage");
        AlembicAPI.addDefaultDamageType("alchemical_damage");
        AlembicAPI.addDefaultDamageType("true_damage");
    }
}
