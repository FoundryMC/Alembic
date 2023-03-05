package foundry.alembic.types.tags;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.AlembicAttribute;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.AlembicTypeModfier;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class AlembicPerLevelTag implements AlembicTag {
    public static final Codec<AlembicPerLevelTag> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("bonus_per_level").forGetter(alembicPerLevelTag -> alembicPerLevelTag.bonusPerLevel),
                    Codec.INT.fieldOf("level_difference").forGetter(alembicPerLevelTag -> alembicPerLevelTag.levelDifference),
                    Codec.FLOAT.fieldOf("max").forGetter(alembicPerLevelTag -> alembicPerLevelTag.cap),
                    AlembicTypeModfier.CODEC.fieldOf("attribute_type").forGetter(alembicPerLevelTag -> alembicPerLevelTag.attrType)
            ).apply(instance, AlembicPerLevelTag::new)
    );

    private final float bonusPerLevel;
    private final int levelDifference;
    private final float cap;
    private final AlembicTypeModfier attrType;
    private AlembicAttribute affectedType;

    public AlembicPerLevelTag(float bonusPerLevel, int levelDifference, float cap, AlembicTypeModfier attrType) {
        this.bonusPerLevel = bonusPerLevel;
        this.levelDifference = levelDifference;
        this.cap = cap;
        this.attrType = attrType;
    }

    @Override
    public void run(ComposedData data) {

    }

    @Override
    public void run(Level level, LivingEntity entity, float damage, DamageSource originalSource) {

    }

    public AlembicAttribute getAffectedType() {
        return affectedType;
    }

    @Override
    public AlembicTagType<?> getType() {
        return null;
    }

    @Override
    public void handlePostParse(AlembicDamageType damageType) {
        this.affectedType = attrType.getAttribute(damageType);
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