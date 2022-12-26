package foundry.alembic.types.tags;

import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;


public class AlembicNoParticleTag implements AlembicTag<Level, Entity> {
    public AlembicNoParticleTag(ResourceLocation particleType, Vector3f color, float alpha) {
    }
    @Override
    public void run(Level level, Entity entity) {

    }

    @Override
    public void run(Level level, LivingEntity entity) {

    }

    @Override
    public String toString() {
        return "AlembicNoParticleTag";
    }
}
