package foundry.alembic.types.tag.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.AlembicTypeModifier;
import foundry.alembic.types.tag.AbstractTag;
import foundry.alembic.types.tag.AlembicTagType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.util.ComposedData;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AlembicPerLevelTag extends AbstractTag {
    public static final Codec<AlembicPerLevelTag> CODEC = RecordCodecBuilder.create(instance ->
            createBase(instance).and(
                    instance.group(
                            Codec.FLOAT.fieldOf("bonus_per_level").forGetter(alembicPerLevelTag -> alembicPerLevelTag.bonusPerLevel),
                            Codec.INT.fieldOf("level_difference").forGetter(alembicPerLevelTag -> alembicPerLevelTag.levelDifference),
                            Codec.FLOAT.fieldOf("max").forGetter(alembicPerLevelTag -> alembicPerLevelTag.cap),
                            AlembicTypeModifier.CODEC.fieldOf("modifier_type").forGetter(alembicPerLevelTag -> alembicPerLevelTag.attrType)
                    )
            ).apply(instance, AlembicPerLevelTag::new)
    );

    private final float bonusPerLevel;
    private final int levelDifference;
    private final float cap;
    private final AlembicTypeModifier attrType;
    private RangedAttribute affectedAttribute;

    public AlembicPerLevelTag(Set<TagCondition> conditions, float bonusPerLevel, int levelDifference, float cap, AlembicTypeModifier attrType) {
        super(conditions);
        this.bonusPerLevel = bonusPerLevel;
        this.levelDifference = levelDifference;
        this.cap = cap;
        this.attrType = attrType;
    }

    @Override
    public void onDamage(ComposedData data) {

    }

    @Override
    public @NotNull AlembicTagType<?> getType() {
        return AlembicTagType.LEVEL_UP;
    }

    @Override
    public void handlePostParse(AlembicDamageType damageType) {
        this.affectedAttribute = attrType.getAffectedAttribute(damageType);
        AlembicGlobalTagPropertyHolder.addLevelupBonus(this);
    }

    public RangedAttribute getAffectedAttribute() {
        return affectedAttribute;
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "Bonus per level: %s, Level difference: %s, Max value: %s, Affected attribute: %s".formatted(bonusPerLevel, levelDifference, cap, affectedAttribute.descriptionId);
    }
}