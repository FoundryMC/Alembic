package foundry.alembic.client;

import foundry.alembic.Alembic;
import foundry.alembic.AlembicConfig;
import foundry.alembic.types.AlembicDamageType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class AlembicOverlayRegistry {
    public static Map<String, ResourceLocation> OVERLAYS = new HashMap<>();

    public static void init(){
        for(String type : AlembicConfig.list.get()){
            OVERLAYS.put(type, Alembic.location("textures/gui/" + type + "_hearts.png"));
        }
    }
}
