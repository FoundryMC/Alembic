package foundry.alembic.types.tags;

import foundry.alembic.Alembic;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class AlembicTagRegistry<T extends AlembicTag> {
    public static final Map<ResourceLocation, AlembicTagType<?>> TAGS = new HashMap<>();

    public static void init(){
        register(Alembic.location("no_particle_tag"), AlembicTagType.NO_PARTICLE);
        register(Alembic.location("particle_tag"), AlembicTagType.PARTICLE);
        register(Alembic.location("extend_fire_tag"), AlembicTagType.EXTEND_FIRE);
        register(Alembic.location("per_level_tag"), new AlembicPerLevelTag(1,1,1,"shielding"));
    }

    public static void register(ResourceLocation name, AlembicTagType<?> type) {
        TAGS.put(name, type);
    }

    public static boolean isRegistered(String name) {
        return TAGS.containsKey(name);
    }

    public static ResourceLocation[] getRegisteredTags() {
        return TAGS.keySet().toArray(ResourceLocation[]::new);
    }
}
