package foundry.alembic.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.util.CodecUtil;
import net.minecraft.core.Registry;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class ItemStatAttributeData {
    public static final Codec<ItemStatAttributeData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(ItemStatAttributeData::getAttribute),
                Codec.FLOAT.fieldOf("value").forGetter(ItemStatAttributeData::getValue),
                CodecUtil.OPERATION_CODEC.fieldOf("operation").forGetter(ItemStatAttributeData::getOperation),
                ExtraCodecs.stringResolverCodec(UUID::toString, UUID::fromString).fieldOf("uuid").forGetter(ItemStatAttributeData::getUUID)

        ).apply(instance, ItemStatAttributeData::new)
    );

    private final Attribute attribute;
    private final float value;
    private final AttributeModifier.Operation operation;
    private final UUID uuid;

    public ItemStatAttributeData(Attribute attribute, float value, AttributeModifier.Operation operation, UUID uuid) {
        this.attribute = attribute;
        this.value = value;
        this.operation = operation;
        this.uuid = uuid;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public UUID getUUID() {
        return uuid;
    }

    public float getValue() {
        return value;
    }

    public AttributeModifier.Operation getOperation() {
        return operation;
    }

    public AttributeModifier createModifier() {
        return new AttributeModifier(uuid, attribute.descriptionId, value, operation);
    }
}
