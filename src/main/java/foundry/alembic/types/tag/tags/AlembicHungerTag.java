package foundry.alembic.types.tag.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.AlembicTypeModifier;
import foundry.alembic.types.tag.AbstractTag;
import foundry.alembic.types.tag.AlembicTagType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.util.CodecUtil;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.util.ComposedData;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class AlembicHungerTag extends AbstractTag {
    public static final Codec<AlembicHungerTag> CODEC = RecordCodecBuilder.create(instance ->
            createBase(instance).and(
                    instance.group(
                            AlembicTypeModifier.CODEC.fieldOf("modifier_type").forGetter(AlembicHungerTag::getTypeModifier),
                            Codec.INT.fieldOf("hunger_amount").forGetter(alembicHungerTag -> alembicHungerTag.hungerTrigger),
                            Codec.FLOAT.fieldOf("amount").forGetter(alembicHungerTag -> alembicHungerTag.scaleAmount),
                            ExtraCodecs.stringResolverCodec(UUID::toString, UUID::fromString).fieldOf("uuid").forGetter(alembicHungerTag -> alembicHungerTag.uuid),
                            CodecUtil.OPERATION_CODEC.fieldOf("operation").forGetter(alembicHungerTag -> alembicHungerTag.operation)
                    )
            ).apply(instance, AlembicHungerTag::new)
            );
    private final int hungerTrigger;
    private final float scaleAmount;

    private final AlembicTypeModifier attribute;

    private final UUID uuid;

    private final AttributeModifier.Operation operation;

    public AlembicHungerTag(Set<TagCondition> conditions, AlembicTypeModifier attribute, int hungerTrigger, float scaleAmount, UUID uuid, AttributeModifier.Operation operation) {
        super(conditions);
        this.hungerTrigger = hungerTrigger;
        this.scaleAmount = scaleAmount;
        this.attribute = attribute;
        this.uuid = uuid;
        this.operation = operation;
    }
    @Override
    public void onDamage(ComposedData data) {

    }

    @Override
    public @NotNull AlembicTagType<?> getType() {
        return AlembicTagType.HUNGER;
    }

    public AlembicTypeModifier getTypeModifier() {
        return attribute;
    }

    public int getHungerTrigger() {
        return hungerTrigger;
    }

    public float getScaleAmount() {
        return scaleAmount;
    }

    public UUID getUUID() {
        return uuid;
    }

    public AttributeModifier.Operation getOperation() {
        return operation;
    }

    @Override
    public void handlePostParse(AlembicDamageType damageType) {
        AlembicGlobalTagPropertyHolder.addHungerBonus(damageType, this);
    }
}
