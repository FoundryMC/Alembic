package foundry.alembic;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.mojang.logging.LogUtils;
import foundry.alembic.client.AlembicOverlayRegistry;
import foundry.alembic.networking.AlembicPacketHandler;
import foundry.alembic.particle.AlembicParticleRegistry;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import foundry.alembic.types.potion.AlembicPotionDataHolder;
import foundry.alembic.types.potion.AlembicPotionRegistry;
import foundry.alembic.types.tags.AlembicTagRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mod(Alembic.MODID)
public class Alembic {
    public static final String MODID = "alembic";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Alembic() {
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
        DamageTypeRegistry.DAMAGE_ATTRIBUTES.register(modEventBus);
        DamageTypeRegistry.DEFENSIVE_ATTRIBUTES.register(modEventBus);
        AlembicParticleRegistry.PARTICLE_TYPES.register(modEventBus);
        AlembicPotionRegistry.MOB_EFFECTS.register(modEventBus);
        AlembicPotionRegistry.POTIONS.register(modEventBus);
        DamageTypeRegistry.init();
        AlembicParticleRegistry.init();
        AlembicOverlayRegistry.init();
        AlembicPacketHandler.init();
        AlembicPotionRegistry.init();
    }

    public static ResourceLocation location(String name) {
        return new ResourceLocation(MODID, name);
    }

    private static void setupConfig(){
        for (String s : AlembicConfig.damageTypes.get()) {
            LOGGER.info("Registered Damage Type: " + s);
            ResourceLocation id = Alembic.location(s);
            if(s.equals("physical_damage")){
                AlembicDamageType damageType = new AlembicDamageType(0, id, 0,0,1,false,false,false, false, 0, new ArrayList<>(), Optional.empty());
                damageType.setResistanceAttribute((RangedAttribute) Attributes.ARMOR);
                DamageTypeRegistry.registerDamageType(id, damageType);
            } else {
                AlembicDamageType damageType = new AlembicDamageType(0, id, 0,0,1,false,false,false, false, 0, new ArrayList<>(), Optional.empty());
                DamageTypeRegistry.registerDamageType(id, damageType);
            }
        }
        for (String s : AlembicConfig.potionEffects.get()) {
            AlembicPotionDataHolder data = new AlembicPotionDataHolder();
            try{
                AlembicPotionRegistry.registerPotionData(s, data);
                LOGGER.info("Registered Potion Effect: " + s);
            } catch (Exception e){
                LOGGER.error("Failed to register Potion Effect: " + s);
            }
        }
    }

    private static void setupDamageTypes() {
        AlembicAPI.addDefaultDamageType("fire_damage");
        AlembicAPI.addDefaultDamageType("arcane_damage");
        AlembicAPI.addDefaultDamageType("alchemical_damage");
        AlembicAPI.addDefaultDamageType("true_damage");
        AlembicAPI.addDefaultDamageType("physical_damage");

        AlembicAPI.addDefaultPotionEffect("fire_damage");
        AlembicAPI.addDefaultPotionEffect("arcane_damage");
        AlembicAPI.addDefaultPotionEffect("alchemical_damage");

        AlembicAPI.addDefaultParticle("true_damage");
        AlembicAPI.addDefaultParticle("physical_damage");
        AlembicAPI.addDefaultParticle("alchemical_damage");
        AlembicAPI.addDefaultParticle("alchemical_reaction");
        AlembicAPI.addDefaultParticle("arcane_damage");
        AlembicAPI.addDefaultParticle("arcane_spark");
        AlembicAPI.addDefaultParticle("fire_damage");
        AlembicAPI.addDefaultParticle("fire_flame");
        AlembicAPI.addDefaultParticle("frostbite");
        AlembicAPI.addDefaultParticle("soul_fire_flame");
        AlembicAPI.addDefaultParticle("wither_decay");
        AlembicAPI.addDefaultParticle("sculk_hit");
    }
}
