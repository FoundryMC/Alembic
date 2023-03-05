package foundry.alembic.types.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.AlembicTypeModfier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.level.Level;

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
    private RangedAttribute affectedType;

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

    @Override
    public AlembicTagType<?> getType() {
        return AlembicTagType.LEVEL_UP;
    }

    @Override
    public void handlePostParse(AlembicDamageType damageType) {
        this.affectedType = attrType.getAttribute(damageType);
        AlembicGlobalTagPropertyHolder.add(this);
    }

    public RangedAttribute getAffectedType() {
        return affectedType;
    }

    public float getBonusPerLevel() {
        return bonusPerLevel;
    }

    public int getLevelDifference() {
        return levelDifference;
    }

    public float getCap() {
        return cap;
    }
}