package foundry.alembic.items.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.items.ItemModifierType;
import foundry.alembic.items.ItemStat;
import foundry.alembic.util.CodecUtil;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

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
}
