package foundry.alembic.types.tags;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;


public interface AlembicTag<L, E, D> {
    void run(L l, E e, D d);
    void run(Level level, LivingEntity entity, float damage);
    String toString();
}
