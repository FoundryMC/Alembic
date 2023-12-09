package foundry.alembic.stats.item.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.stats.item.ItemModifierType;
import foundry.alembic.stats.item.ItemStat;
import foundry.alembic.codecs.CodecUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ReplaceItemModifier implements ItemModifier {
    public static final Codec<ReplaceItemModifier> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ATTRIBUTE.byNameCodec().fieldOf("target").forGetter(replaceItemModifier -> replaceItemModifier.target),
                    Codec.FLOAT.fieldOf("value").forGetter(replaceItemModifier -> replaceItemModifier.value),
                    CodecUtil.OPERATION_CODEC.fieldOf("operation").forGetter(replaceItemModifier -> replaceItemModifier.operation),
                    CodecUtil.STRING_UUID.fieldOf("uuid").forGetter(replaceItemModifier -> replaceItemModifier.uuid)
            ).apply(instance, ReplaceItemModifier::new)
    );

    private final Attribute target;
    private final float value;
    private final AttributeModifier.Operation operation;
    private final UUID uuid;

    public ReplaceItemModifier(Attribute target, float value, AttributeModifier.Operation operation, UUID uuid) {
        this.target = target;
        this.value = value;
        this.operation = operation;
        this.uuid = uuid;
    }

    @Override
    public void compute(ItemStat.AttributeContainer container) {
        if (container.contains(target)) {
            container.remove(target);
            container.put(target, new AttributeModifier(uuid, target.descriptionId, value, operation));
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
    public @Nullable UUID getUUID() {
        return uuid;
    }
}
