package foundry.alembic.types.tags;

import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;


public class AlembicNoParticleTag implements AlembicTag {
    public static final Supplier<AlembicNoParticleTag> INSTANCE = Suppliers.memoize(AlembicNoParticleTag::new);
    public static final Codec<AlembicNoParticleTag> CODEC = Codec.unit(INSTANCE);

    public AlembicNoParticleTag() {
    }


    @Override
    public void run(ComposedData data) {

    }

    @Override
    public void run(Level level, LivingEntity entity, float damage, DamageSource originalSource) {

    }

    @Override
    public AlembicTagType<?> getType() {
        return AlembicTagType.NO_PARTICLE;
    }

    @Override
    public String toString() {
        return "AlembicNoParticleTag";
    }
}
