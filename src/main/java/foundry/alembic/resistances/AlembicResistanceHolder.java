package foundry.alembic.resistances;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.entity.EntityType;

import java.util.*;

public class AlembicResistanceHolder {
    private static final Map<EntityType<?>, AlembicResistance> RESISTANCE_MAP = new Reference2ObjectOpenHashMap<>();

    public static Collection<AlembicResistance> getValuesView() {
        return Collections.unmodifiableCollection(RESISTANCE_MAP.values());
    }

    private static void put(AlembicResistance resistance){
        RESISTANCE_MAP.put(resistance.getEntityType(), resistance);
    }


    static void clear() {
        RESISTANCE_MAP.clear();
    }

    public static AlembicResistance get(EntityType<?> entityType){
        return RESISTANCE_MAP.get(entityType);
    }

    public static void smartAddResistance(AlembicResistance resistance){
        if(!RESISTANCE_MAP.containsKey(resistance.getEntityType()) || get(resistance.getEntityType()).getPriority() < resistance.getPriority()) {
            put(resistance);
        }
    }
}
