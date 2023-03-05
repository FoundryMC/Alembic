package foundry.alembic;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import com.mojang.logging.LogUtils;
import foundry.alembic.client.AlembicOverlayRegistry;
import foundry.alembic.networking.AlembicPacketHandler;
import foundry.alembic.particle.AlembicParticleRegistry;
import foundry.alembic.types.AlembicAttribute;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import foundry.alembic.types.tags.AlembicTagRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Alembic.MODID)
public class Alembic {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "alembic";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public Alembic() {
        MixinExtrasBootstrap.init();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        AlembicTagRegistry.init();
        setupDamageTypes();
        ForgeConfigSpec spec = AlembicConfig.makeConfig(new ForgeConfigSpec.Builder());

        final CommentedFileConfig file = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve("alembic-common.toml"))
                .sync().autosave().writingMode(WritingMode.REPLACE).build();
        file.load();
        List<String> types = file.get("general.types");
        spec.setConfig(file);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, spec);
        setupConfig();
        MinecraftForge.EVENT_BUS.register(this);
        DamageTypeRegistry.DAMAGE_ATTRIBUTES.register(modEventBus);
        DamageTypeRegistry.DEFENSIVE_ATTRIBUTES.register(modEventBus);
        AlembicParticleRegistry.PARTICLE_TYPES.register(modEventBus);
        DamageTypeRegistry.init();
        AlembicParticleRegistry.init();
        AlembicOverlayRegistry.init();
        AlembicPacketHandler.init();
    }

    public static ResourceLocation location(String name) {
        return new ResourceLocation(MODID, name);
    }
    public static void setupConfig(){
        for (String s : AlembicConfig.list.get()) {
            LOGGER.info("Registered Damage Type: " + s);
            if(s.equals("physical_damage")){
                AlembicDamageType damageType = new AlembicDamageType(0, Alembic.location(s), 0,0,1,false,false,false, 0, false);
                damageType.setShieldAttribute((RangedAttribute) Attributes.ARMOR);
                DamageTypeRegistry.registerDamageType(damageType);
            } else {
                AlembicDamageType damageType = new AlembicDamageType(0, Alembic.location(s), 0,0,1,false,false,false, 0, false);
                DamageTypeRegistry.registerDamageType(damageType);
            }
        }
    }

    public static void setupDamageTypes(){
        AlembicAPI.addDefaultDamageType("fire_damage");
        AlembicAPI.addDefaultDamageType("arcane_damage");
        AlembicAPI.addDefaultDamageType("alchemical_damage");
        AlembicAPI.addDefaultDamageType("true_damage");
        AlembicAPI.addDefaultDamageType("physical_damage");
    }
}
