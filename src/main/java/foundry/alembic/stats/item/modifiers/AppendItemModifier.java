package foundry.alembic.stats.item.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.attribute.UUIDManager;
import foundry.alembic.stats.item.ItemModifierType;
import foundry.alembic.stats.item.ItemStat;
import foundry.alembic.codecs.CodecUtil;
import foundry.alembic.stats.item.slots.EquipmentSlotType;
import foundry.alembic.util.AttributeHelper;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public final class AppendItemModifier implements ItemModifier {
    public static final Codec<AppendItemModifier> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ATTRIBUTE.byNameCodec().fieldOf("attribute").forGetter(AppendItemModifier::getAttribute),
                    Codec.FLOAT.fieldOf("value").forGetter(AppendItemModifier::getValue),
                    CodecUtil.OPERATION_CODEC.fieldOf("operation").forGetter(AppendItemModifier::getOperation),
                    CodecUtil.STRING_UUID.optionalFieldOf("uuid").forGetter(AppendItemModifier::getUUID)
            ).apply(instance, AppendItemModifier::new)
    );

    private final Attribute attribute;
    private final float value;
    private final AttributeModifier.Operation operation;
    private final Optional<UUID> uuid;

    public AppendItemModifier(Attribute attribute, float value, AttributeModifier.Operation operation, Optional<UUID> uuid) {
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
    public Optional<UUID> getUUID() {
        return uuid;
    }

    public float getValue() {
        return value;
    }

    public AttributeModifier.Operation getOperation() {
        return operation;
    }

    @Override
    public void compute(ItemStat.AttributeContainer container, EquipmentSlotType slotType) {
        UUID usedUuid;
        if (slotType.getVanillaSlot() == null || slotType.getVanillaSlot().getType() == EquipmentSlot.Type.ARMOR) {
            usedUuid = uuid.orElseGet(() -> AttributeHelper.slotUuid(slotType));
        } else {
            usedUuid = uuid.orElseGet(() -> AttributeHelper.baseAttributeUuid(attribute));
        }
        container.put(attribute, new AttributeModifier(usedUuid, attribute.descriptionId, value, operation));
    }

    @Override
    public ItemModifierType getType() {
        return ItemModifierType.APPEND;
    }
}
