package foundry.alembic.types.tags;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;


public interface AlembicTag<T, V> {
    void run(T t, V v);
    void run(Level level, LivingEntity entity);
    String toString();
}
