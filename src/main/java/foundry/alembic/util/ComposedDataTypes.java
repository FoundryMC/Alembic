package foundry.alembic.util;

import foundry.alembic.types.AlembicDamageType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class ComposedDataTypes {
    private ComposedDataTypes() {}

    public static final ComposedDataType<ServerLevel> SERVER_LEVEL = create(ServerLevel.class);
    public static final ComposedDataType<LivingEntity> TARGET_ENTITY = create(LivingEntity.class);
    public static final ComposedDataType<Float> FINAL_DAMAGE = create(Float.class);
    public static final ComposedDataType<DamageSource> ORIGINAL_SOURCE = create(DamageSource.class);
    public static final ComposedDataType<AlembicDamageType> DAMAGE_TYPE = create(AlembicDamageType.class);

    public static <T> ComposedDataType<T> create(Class<T> clazzForType) {
        return new ComposedDataType<>(clazzForType);
    }
}