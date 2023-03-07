package foundry.alembic.resistances;

import net.minecraft.world.entity.EntityType;

import java.util.*;

public class AlembicResistanceHolder {
    private static final Map<EntityType<?>, AlembicResistance> RESISTANCE_MAP = new HashMap<>();

    public static Collection<AlembicResistance> getValuesView() {
        return RESISTANCE_MAP.values();
    }

    public static void add(AlembicResistance resistance){
        RESISTANCE_MAP.put(resistance.getEntityType(), resistance);
    }

    public static void remove(AlembicResistance resistance){
        RESISTANCE_MAP.remove(resistance.getEntityType());
    }

    public static void clear() {
        RESISTANCE_MAP.clear();
    }

    public static AlembicResistance get(EntityType<?> entityType){
        return RESISTANCE_MAP.get(entityType);
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
