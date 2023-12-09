package foundry.alembic.stats.item.modifiers;

import com.mojang.serialization.Codec;
import foundry.alembic.stats.item.ItemModifierType;
import foundry.alembic.stats.item.ItemStat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class RemoveItemModifier implements ItemModifier {
    public static final Codec<RemoveItemModifier> CODEC = BuiltInRegistries.ATTRIBUTE.byNameCodec().fieldOf("attribute")
            .xmap(RemoveItemModifier::new, removeItemModifier -> removeItemModifier.attribute)
            .codec();

    private final Attribute attribute;

    public RemoveItemModifier(Attribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public void compute(ItemStat.AttributeContainer container) {
        container.remove(attribute);
    }

    @Override
    public ItemModifierType getType() {
        return ItemModifierType.REMOVE;
    }

    @Override
    public @Nullable Attribute getAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getTarget() {
        return attribute;
    }

    @Override
    public @Nullable UUID getUUID() {
        return null;
    }
}
