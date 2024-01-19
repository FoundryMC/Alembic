package foundry.alembic.types.tag.condition;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import foundry.alembic.codecs.CodecUtil;
import net.minecraft.resources.ResourceLocation;

public class TagConditionRegistry {
    private static final BiMap<ResourceLocation, TagConditionType<?>> REGISTRY = HashBiMap.create();
    public static final Codec<TagConditionType<?>> CONDITION_LOOKUP_CODEC = CodecUtil.ALEMBIC_RL_CODEC.comapFlatMap(resourceLocation -> {
        if (!REGISTRY.containsKey(resourceLocation)) {
            return DataResult.error(() -> "Damage type condition %s does not exist!".formatted(resourceLocation));
        }
        return DataResult.success(REGISTRY.get(resourceLocation));
    }, REGISTRY.inverse()::get);

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