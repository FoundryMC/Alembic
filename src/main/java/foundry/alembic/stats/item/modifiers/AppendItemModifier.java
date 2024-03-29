package foundry.alembic.stats.item.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.stats.item.ItemModifierType;
import foundry.alembic.stats.item.ItemStat;
import foundry.alembic.codecs.CodecUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import javax.annotation.Nullable;
import java.util.UUID;

public final class AppendItemModifier implements ItemModifier {
    public static final Codec<AppendItemModifier> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ATTRIBUTE.byNameCodec().fieldOf("attribute").forGetter(AppendItemModifier::getAttribute),
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

    @Override
    public Attribute getAttribute() {
        return attribute;
    }

    @Override
    public @Nullable Attribute getTarget() {
        return null;
    }

    @Override
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
