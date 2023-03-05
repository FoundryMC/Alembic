package foundry.alembic.types.tags;

import com.mojang.serialization.Codec;
import foundry.alembic.types.AlembicDamageType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public interface AlembicTag {
    Codec<AlembicTag> CODEC = ResourceLocation.CODEC.dis

    void run(ComposedData data);
    void run(Level level, LivingEntity entity, float damage, DamageSource originalSource);

    AlembicTagType<?> getType();

    /**
     * For handling anything that should be added to a tag after the damage type has been registered
     * @param damageType Registered damage type. Safe to reference
     */
    void handlePostParse(AlembicDamageType damageType);

    //    void handleData(JsonArray data, List<AlembicTag> tags, String tagId, ResourceLocation damageType);

    class ComposedData {
        private final Map<ComposedDataType<?>, Object> data = new HashMap<>();
        public static ComposedData createEmpty() {
            return new ComposedData();
        }

        public <T> ComposedData add(ComposedDataType<T> type, T obj) {
            data.put(type, obj);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> T get(ComposedDataType<T> type) {
            return (T) data.get(type);
        }
    }

    class ComposedDataType<T> {
        private final Class<T> clazz;
        private ComposedDataType(Class<T> clazzForType) {
            this.clazz = clazzForType;
        }

        public static <T> ComposedDataType<T> create(Class<T> clazzForType) {
            return new ComposedDataType<>(clazzForType);
        }

        public static final ComposedDataType<Level> LEVEL = create(Level.class);
        public static final ComposedDataType<Entity> TARGET_ENTITY = create(Entity.class);
    }
}
