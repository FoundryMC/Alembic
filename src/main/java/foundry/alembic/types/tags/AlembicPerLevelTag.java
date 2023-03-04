package foundry.alembic.types.tags;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class AlembicPerLevelTag implements AlembicTag<Level, Entity, Float>{
    AlembicPerLevelDataHolder data;
    public AlembicPerLevelTag(AlembicTagDataHolder data) {
    }
    @Override
    public void run(Level level, Entity entity, Float aFloat) {

    }

    @Override
    public void run(Level level, LivingEntity entity, float damage, DamageSource originalSource) {

    }

    @Override
    public void handleData(JsonArray tagValues, List<AlembicTag<?, ?, ?>> tags, String tagId, ResourceLocation damageType) {
        for (int i = 0; i < tagValues.size(); i++) {
            JsonObject tag = tagValues.get(i).getAsJsonObject();
            float perLevel = tag.get("perLevel").getAsFloat();
            float max = tag.get("max").getAsFloat();
            int levelDifference = tag.get("levelDifference").getAsInt();
            String type = tag.get("type").getAsString();
            AlembicPerLevelDataHolder data = new AlembicPerLevelDataHolder(perLevel, levelDifference, max, type);
            AlembicPerLevelTag perTag = new AlembicPerLevelTag(new AlembicTagDataHolder(data));
            tags.add(perTag);
            if(DamageTypeRegistry.doesDamageTypeExist(damageType)){
                AlembicDamageType dtype = DamageTypeRegistry.getDamageType(damageType);
                if(type.equals("shielding")){
                    AlembicGlobalTagPropertyHolder.LEVELUP_ATTRIBUTES.put(dtype.getShieldAttribute(), data);
                } else if (type.equals("absorption")){
                    AlembicGlobalTagPropertyHolder.LEVELUP_ATTRIBUTES.put(dtype.getAbsorptionAttribute(), data);
                } else if (type.equals("resistance")){
                    AlembicGlobalTagPropertyHolder.LEVELUP_ATTRIBUTES.put(dtype.getResistanceAttribute(), data);
                }
            }
        }
    }

}