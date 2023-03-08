package foundry.alembic.types.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.AlembicDamageType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class AlembicHungerTag implements AlembicTag {
    public static final Codec<AlembicHungerTag> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("attribute").forGetter(alembicHungerTag -> alembicHungerTag.attribute),
                    Codec.INT.fieldOf("hunger_amount").forGetter(alembicHungerTag -> alembicHungerTag.hungerTrigger),
                    Codec.FLOAT.fieldOf("amount").forGetter(alembicHungerTag -> alembicHungerTag.scaleAmount),
                    Codec.STRING.fieldOf("uuid").forGetter(alembicHungerTag -> alembicHungerTag.uuid.toString()),
                    Codec.STRING.fieldOf("operation").forGetter(alembicHungerTag -> alembicHungerTag.operation.name())
                    ).apply(instance, AlembicHungerTag::new)
            );
    private final int hungerTrigger;
    private final float scaleAmount;

    private final String attribute;

    private final UUID uuid;

    private final AttributeModifier.Operation operation;

    public AlembicHungerTag(String attribute, int hungerTrigger, float scaleAmount, String uuid, String operation) {
        this.hungerTrigger = hungerTrigger;
        this.scaleAmount = scaleAmount;
        this.attribute = attribute;
        this.uuid = UUID.fromString(uuid);
        this.operation = AttributeModifier.Operation.valueOf(operation);
    }
    @Override
    public void run(ComposedData data) {

    }

    @Override
    public AlembicTagType<?> getType() {
        return null;
    }

    public String getAttribute() {
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
