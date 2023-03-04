package foundry.alembic.types.tags;

import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class AlembicTagRegistry<T extends AlembicTag<?,?,?>> {
    public static Map<String, AlembicTagType> TAGS = new HashMap<>();
    public static Map<String, AlembicTag<?,?,?>>  STATIC_INSTANCES = new HashMap<>();

    public static void init(){
        register("AlembicNoParticleTag", new AlembicTagType(AlembicNoParticleTag.class, AlembicTagDataHolder.class), new AlembicNoParticleTag(new AlembicTagDataHolder()));
        register("AlembicParticleTag", new AlembicTagType(AlembicParticleTag.class, AlembicTagDataHolder.class), new AlembicParticleTag(new AlembicTagDataHolder("minecraft:dust", new Vector3f(1,1,1), 1f)));
        register("AlembicExtendFireTag", new AlembicTagType(AlembicExtendFireTag.class, AlembicTagDataHolder.class), new AlembicExtendFireTag(new AlembicTagDataHolder(0.25f, List.of(""))));
        register("AlembicPerLevelTag", new AlembicTagType(AlembicPerLevelTag.class, AlembicTagDataHolder.class), new AlembicPerLevelTag(new AlembicTagDataHolder(new AlembicPerLevelDataHolder(1,1,1,"shielding"))));
    }

    public static void register(String name, AlembicTagType type) {
        TAGS.put(name, type);
    }

    public static void register(String name, AlembicTagType type, AlembicTag<?,?,?> instance) {
        TAGS.put(name, type);
        STATIC_INSTANCES.put(name, instance);
    }

    public static boolean isRegistered(String name) {
        return TAGS.containsKey(name);
    }

    public static <T extends AlembicTag<?,?,?>> T create(String name, AlembicTagDataHolder data) {
        AlembicTagType type = TAGS.get(name);
        if (type == null) {
            return null;
        }
        try {
            Constructor<?> constructor = type.clazz().getConstructor(type.args());
            return (T) constructor.newInstance(data);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getRegisteredTags() {
        return TAGS.keySet().toArray(new String[0]);
    }
}
