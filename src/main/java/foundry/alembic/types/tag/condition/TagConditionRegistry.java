package foundry.alembic.types.tag.condition;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import foundry.alembic.util.CodecUtil;
import net.minecraft.resources.ResourceLocation;

public class TagConditionRegistry {
    public static final BiMap<ResourceLocation, TagConditionType<?>> REGISTRY = HashBiMap.create();
    public static final Codec<TagConditionType<?>> CONDITION_LOOKUP_CODEC = CodecUtil.ALEMBIC_RL_CODEC.xmap(REGISTRY::get, REGISTRY.inverse()::get);

    public static void init() {
        TagConditionType.bootstrap();
    }

    public static void register(ResourceLocation id, TagConditionType<?> type) {
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("TagCondition with id: " + id + " already registered.");
        }

        REGISTRY.put(id, type);
    }
}
