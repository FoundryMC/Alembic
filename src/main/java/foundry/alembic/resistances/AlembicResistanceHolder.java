package foundry.alembic.resistances;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlembicResistanceHolder {
    private static final List<AlembicResistance> RESISTANCE_MAP = new ArrayList<>();

    public static List<AlembicResistance> getResistanceMap() {
        return RESISTANCE_MAP;
    }

    public static void add(AlembicResistance resistance){
        RESISTANCE_MAP.add(resistance);
    }

    public static void remove(AlembicResistance resistance){
        RESISTANCE_MAP.remove(resistance);
    }

    public static AlembicResistance get(EntityType<?> entityType){
        for (AlembicResistance resistance : RESISTANCE_MAP){
            if (resistance.getEntityType() == entityType){
                return resistance;
            }
        }
        return null;
    }

    public static void smartAddResistance(AlembicResistance resistance){
        AlembicResistance existing = get(resistance.getEntityType());
        if(existing == null){
            add(resistance);
        } else {
            if(existing.getPriority() < resistance.getPriority()){
                remove(existing);
                add(resistance);
            }
        }
    }
}
