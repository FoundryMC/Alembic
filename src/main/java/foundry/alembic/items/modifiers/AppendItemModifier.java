package foundry.alembic.items.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.items.ItemModifier;
import foundry.alembic.items.ItemModifierType;
import foundry.alembic.items.ItemStat;
import foundry.alembic.util.CodecUtil;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class AppendItemModifier implements ItemModifier {
    public static final Codec<AppendItemModifier> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Registry.ATTRIBUTE.byNameCodec().fieldOf("attribute").forGetter(AppendItemModifier::getAttribute),
                    Codec.FLOAT.fieldOf("value").forGetter(AppendItemModifier::getValue),
                    CodecUtil.OPERATION_CODEC.fieldOf("operation").forGetter(AppendItemModifier::getOperation),
                    CodecUtil.STRING_UUID.fieldOf("uuid").forGetter(AppendItemModifier::getUUID)
            ).apply(instance, AppendItemModifier::new)
    );

    private final Attribute attribute;
    private final float value;
    private final AttributeModifier.Operation operation;
    private final UUID uuid;

    public AppendItemModifier(Attribute attribute, float value, AttributeModifier.Operation operation, UUID uuid) {
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

    @Override
    public void compute(ItemStat.AttributeContainer container) {
        container.put(attribute, new AttributeModifier(uuid, attribute.descriptionId, value, operation));
    }

    @Override
    public ItemModifierType getType() {
        return ItemModifierType.APPEND;
    }
}
