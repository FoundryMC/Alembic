package foundry.alembic.types.tags;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Vector3f;
import foundry.alembic.Alembic;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AlembicParticleTag implements AlembicTag<Level, Entity, Integer> {
    public ResourceLocation particleType;
    public Vector3f color;
    public float alpha;
    public AlembicParticleTag(AlembicTagDataHolder data){
        this.particleType = ResourceLocation.tryParse((String)data.data.get(0));
        this.color = (Vector3f) data.data.get(1);
        this.alpha = (float) data.data.get(2);
    }


    @Override
    public void run(Level level, Entity entity, Integer damage) {

    }

    @Override
    public void run(Level level, LivingEntity entity, float damage, DamageSource originalSource) {
        if(particleType.getPath().equals("dust")){
            ((ServerLevel) level).sendParticles(new DustParticleOptions(color, alpha), entity.getX(), entity.getY() + entity.getBbHeight()/2f, entity.getZ(),
                    25,
                    level.random.nextFloat()-0.5f,
                    level.random.nextFloat()-0.5f,
                    level.random.nextFloat()-0.5f,
                    0.15f);
            return;
        }
        SimpleParticleType type = (SimpleParticleType)ForgeRegistries.PARTICLE_TYPES.getValue(particleType);
        if(type == null) return;
        ((ServerLevel) level).sendParticles(type, entity.getX(), entity.getY() + entity.getBbHeight()/2f, entity.getZ(),
                (int) Math.ceil(damage),
                level.random.nextFloat()-0.5f,
                level.random.nextFloat()-0.5f,
                level.random.nextFloat()-0.5f,
                0.15f);
    }

    @Override
    public String toString() {
        return "AlembicParticleTag";
    }

    @Override
    public void handleData(JsonArray tagValues, List<AlembicTag<?,?,?>> tags, String tagId, ResourceLocation damageType) {
        for (int i = 0; i < tagValues.size(); i++) {
            //Alembic.LOGGER.error(tagValues.get(i).getAsString());
            if(tagValues.get(i).isJsonObject()){
                JsonObject particleJson = tagValues.get(i).getAsJsonObject();
                AtomicReference<Vector3f> color = new AtomicReference<>(new Vector3f(1, 1, 1));
                AtomicReference<Float> alpha = new AtomicReference<>((float) 1);
                AtomicReference<String> particleId = new AtomicReference<>("");
                particleJson.keySet().forEach(key -> {
                    particleId.set(key);
                    JsonArray particleValues = particleJson.get(key).getAsJsonArray();
                    color.set(new Vector3f(particleValues.get(0).getAsFloat(), particleValues.get(1).getAsFloat(), particleValues.get(2).getAsFloat()));
                    alpha.set(particleValues.get(3).getAsFloat());
                });
                AlembicTagDataHolder dataHolder = new AlembicTagDataHolder(particleId.get(), color.get(), alpha.get());
                if(AlembicTagRegistry.isRegistered(tagId)){
                    Alembic.LOGGER.debug("Registering tag {} for damage type {}", tagId, damageType);
                    tags.add(AlembicTagRegistry.create(tagId, dataHolder));
                } else {
                    Alembic.LOGGER.error("Tag {} is not registered! Valid tags are {}", tagId, AlembicTagRegistry.getRegisteredTags());
                }
            }
        }
    }
}
