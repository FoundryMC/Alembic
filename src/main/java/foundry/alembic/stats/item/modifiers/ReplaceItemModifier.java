package foundry.alembic.stats.item.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.attribute.UUIDManager;
import foundry.alembic.stats.item.ItemModifierType;
import foundry.alembic.stats.item.ItemStat;
import foundry.alembic.codecs.CodecUtil;
import foundry.alembic.stats.item.slots.EquipmentSlotType;
import foundry.alembic.util.AttributeHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class ReplaceItemModifier implements ItemModifier {
    public static final Codec<ReplaceItemModifier> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ATTRIBUTE.byNameCodec().fieldOf("target").forGetter(ReplaceItemModifier::getTarget),
                    Codec.FLOAT.fieldOf("value").forGetter(replaceItemModifier -> replaceItemModifier.value),
                    CodecUtil.OPERATION_CODEC.fieldOf("operation").forGetter(replaceItemModifier -> replaceItemModifier.operation),
                    CodecUtil.STRING_UUID.optionalFieldOf("uuid").forGetter(ReplaceItemModifier::getUUID)
            ).apply(instance, ReplaceItemModifier::new)
    );

    private final Attribute target;
    private final float value;
    private final AttributeModifier.Operation operation;
    private final Optional<UUID> uuid;

    public ReplaceItemModifier(Attribute target, float value, AttributeModifier.Operation operation, Optional<UUID> uuid) {
        this.target = target;
        this.value = value;
        this.operation = operation;
        this.uuid = uuid;
    }

    @Override
    public void compute(ItemStat.AttributeContainer container, EquipmentSlotType slotType) {
        if (container.contains(target)) {
            container.remove(target);
            UUID usedUuid;
            if (slotType.getVanillaSlot() == null || slotType.getVanillaSlot().getType() == EquipmentSlot.Type.ARMOR) {
                usedUuid = uuid.orElseGet(() -> AttributeHelper.slotUuid(slotType));
            } else {
                usedUuid = uuid.orElseGet(() -> AttributeHelper.baseAttributeUuid(target));
            }
            container.put(target, new AttributeModifier(usedUuid, target.descriptionId, value, operation));
        }
    }

    @Override
    public ItemModifierType getType() {
        return ItemModifierType.REPLACE;
    }

    @Override
    public @Nullable Attribute getAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getTarget() {
        return target;
    }

    @Override
    public @Nullable Optional<UUID> getUUID() {
        return uuid;
    }
}
