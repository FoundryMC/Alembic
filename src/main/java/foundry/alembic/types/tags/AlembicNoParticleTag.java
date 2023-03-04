package foundry.alembic.types.tags;

import com.google.gson.JsonArray;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;


public class AlembicNoParticleTag implements AlembicTag<Level, Entity, Integer> {
    public AlembicNoParticleTag(AlembicTagDataHolder data) {
    }


    @Override
    public void run(Level level, Entity entity, Integer integer) {

    }

    @Override
    public void run(Level level, LivingEntity entity, float damage, DamageSource originalSource) {

    }

    @Override
    public String toString() {
        return "AlembicNoParticleTag";
    }

    @Override
    public void handleData(JsonArray data, List<AlembicTag<?, ?, ?>> tags, String tagId, ResourceLocation damageType) {
        for(int i = 0; i < data.size(); i++){
            tags.add(new AlembicNoParticleTag(new AlembicTagDataHolder(data.get(i).getAsJsonObject())));
        }
    }
}
