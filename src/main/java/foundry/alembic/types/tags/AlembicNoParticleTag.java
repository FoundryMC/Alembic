package foundry.alembic.types.tags;

import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;


public class AlembicNoParticleTag implements AlembicTag<Level, Entity, Integer> {
    public AlembicNoParticleTag(ResourceLocation particleType, Vector3f color, float alpha) {
    }


    @Override
    public void run(Level level, Entity entity, Integer integer) {

    }

    @Override
    public void run(Level level, LivingEntity entity, float damage) {

    }

    @Override
    public String toString() {
        return "AlembicNoParticleTag";
    }
}
