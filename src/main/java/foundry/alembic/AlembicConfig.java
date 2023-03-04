package foundry.alembic;

import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


// TODO: figure out how to add existing damage types to the config without regenerating the whole thingggggggggggggggggggggg
public class AlembicConfig {
    public static ForgeConfigSpec.ConfigValue<List<String>> list;


    public static ForgeConfigSpec makeConfig(ForgeConfigSpec.Builder builder){
        builder.comment("General settings").push("general");
        list = builder.comment("List of config-initialized damage types").define("types", AlembicAPI.getDefaultDamageTypes());
        return builder.build();
    }

    public List<String> getList(){
        return AlembicConfig.list.get();
    }

    public static void addDamageType(String name){
        AlembicConfig.list.get().add(name);
    }
}
