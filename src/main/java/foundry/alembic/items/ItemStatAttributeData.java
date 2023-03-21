package foundry.alembic.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.CodecUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class ItemStatAttributeData {
    public static final Codec<ItemStatAttributeData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.STRING.fieldOf("attribute").forGetter(ItemStatAttributeData::getAttribute),
                Codec.FLOAT.fieldOf("value").forGetter(ItemStatAttributeData::getValue),
                Codec.STRING.fieldOf("operation").forGetter(ItemStatAttributeData::getOperation),
                ExtraCodecs.UUID.fieldOf("uuid").forGetter(ItemStatAttributeData::getUUID)

        ).apply(instance, ItemStatAttributeData::new)
    );

    private final String attribute;
    private final float value;
    private final AttributeModifier.Operation operation;

    private final UUID uuid;

    public ItemStatAttributeData(String attribute, float value, String operation, UUID uuid) {
        this.attribute = attribute;
        this.value = value;
        this.operation = AttributeModifier.Operation.valueOf(operation);
        this.uuid = uuid;
    }

    public String getAttribute() {
        return attribute;
    }

    public UUID getUUID() {
        return uuid;
    }

    public float getValue() {
        return value;
    }

    public String getOperation() {
        return operation.name();
    }

    public AttributeModifier.Operation getOperationEnum() {
        return operation;
    }
}
