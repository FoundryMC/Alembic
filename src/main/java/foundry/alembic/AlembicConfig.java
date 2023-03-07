package foundry.alembic;

import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


// TODO: figure out how to add existing damage types to the config without regenerating the whole thingggggggggggggggggggggg
public class AlembicConfig {
    public static ForgeConfigSpec.ConfigValue<? extends List<String>> list;
    public static ForgeConfigSpec.ConfigValue<? extends List<String>> potionEffects;

    public static ForgeConfigSpec.ConfigValue<? extends List<String>> particles;


    public static ForgeConfigSpec makeConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("General settings").push("general");
        list = builder.comment("List of config-initialized damage types").define("types", AlembicAPI.getDefaultDamageTypes());
        potionEffects = builder.comment("List of config-initialized potion effects").define("potion_effects", AlembicAPI.getDefaultPotionEffects());
        particles = builder.comment("List of config-initialized particles").define("particles", AlembicAPI.getDefaultParticles());
        return builder.build();
    }

    public List<String> getList() {
        return AlembicConfig.list.get();
    }

    public static void addDamageType(String name) {
        AlembicConfig.list.get().add(name);
    }
}
