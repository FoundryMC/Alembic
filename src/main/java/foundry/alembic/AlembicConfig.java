package foundry.alembic;

import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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

    public void addDamageType(String name){
        AlembicConfig.list.get().add(name);
    }
}
