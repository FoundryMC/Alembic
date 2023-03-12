package foundry.alembic.types.tags;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import foundry.alembic.Alembic;
import foundry.alembic.CodecUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class AlembicTagRegistry<T extends AlembicTag> {
    public static final BiMap<ResourceLocation, AlembicTagType<?>> TAGS = HashBiMap.create();

    public static final Codec<AlembicTagType<?>> TAG_MAP_CODEC = CodecUtil.ALEMBIC_RL_CODEC.xmap(TAGS::get, TAGS.inverse()::get);

    public static void init(){
        register(Alembic.location("no_particle_tag"), AlembicTagType.NO_PARTICLE);
        register(Alembic.location("particle_tag"), AlembicTagType.PARTICLE);
        register(Alembic.location("extend_fire_tag"), AlembicTagType.EXTEND_FIRE);
        register(Alembic.location("per_level_tag"), AlembicTagType.LEVEL_UP);
        register(Alembic.location("hunger_tag"), AlembicTagType.HUNGER);
    }

    public static void register(ResourceLocation name, AlembicTagType<?> type) {
        TAGS.put(name, type);
    }

    public static boolean isRegistered(String name) {
        ResourceLocation id = name.contains(":") ? new ResourceLocation(name) : Alembic.location(name);
        return TAGS.containsKey(id);
    }

    public static ResourceLocation[] getRegisteredTags() {
        return TAGS.keySet().toArray(ResourceLocation[]::new);
    }
}
