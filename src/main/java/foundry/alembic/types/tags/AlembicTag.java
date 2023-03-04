package foundry.alembic.types.tags;

import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;


public interface AlembicTag<L, E, D> {
    void run(L l, E e, D d);
    void run(Level level, LivingEntity entity, float damage, DamageSource originalSource);
    String toString();

    void handleData(JsonArray data, List<AlembicTag<?,?,?>> tags, String tagId, ResourceLocation damageType);
}
