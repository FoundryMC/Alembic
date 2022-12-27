package foundry.alembic.types.tags;

import com.mojang.math.Vector3f;
import foundry.alembic.Alembic;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class AlembicTagRegistry<T extends AlembicTag<?,?>> {
    public static Map<String, AlembicTagType> TAGS = new HashMap<>();

    public static void init(){
        TAGS.put("AlembicNoParticleTag", new AlembicTagType(AlembicNoParticleTag.class));
        TAGS.put("AlembicParticleTag", new AlembicTagType(AlembicParticleTag.class, ResourceLocation.class, Vector3f.class, Float.TYPE));
    }

    public static void register(String name, AlembicTagType type) {
        TAGS.put(name, type);
    }

    public static boolean isRegistered(String name) {
        return TAGS.containsKey(name);
    }

    public static <T extends AlembicTag<?,?>> T create(String name, AlembicTagDataHolder data) {
        AlembicTagType type = TAGS.get(name);
        if (type == null) {
            return null;
        }
        try {
            Constructor<?> constructor = type.getClazz().getConstructor(type.getArgs());
            ResourceLocation RL = ResourceLocation.tryParse((String)data.data.get(0));
            if(RL.getPath().equals("dust")){
                Vector3f color = (Vector3f) data.data.get(1);
                float alpha = (float) data.data.get(2);
                return (T) constructor.newInstance(RL, color, alpha);
            } else {
                return (T) constructor.newInstance(RL, Vector3f.ZERO, 1.0f);
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getRegisteredTags() {
        return TAGS.keySet().toArray(new String[0]);
    }
}
