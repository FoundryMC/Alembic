package foundry.alembic.client;

import foundry.alembic.Alembic;
import foundry.alembic.AlembicConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class AlembicOverlayRegistry {
    public static Map<String, ResourceLocation> OVERLAYS = new HashMap<>();

    public static void init(){
//        for(String type : AlembicConfig
//                .damageTypes.get()){
//            OVERLAYS.put(type, Alembic.location("textures/gui/" + type + "_hearts.png"));
//        }
    }
}
