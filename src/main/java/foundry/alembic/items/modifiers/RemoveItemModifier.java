package foundry.alembic.items.modifiers;

import com.mojang.serialization.Codec;
import foundry.alembic.items.ItemModifierType;
import foundry.alembic.items.ItemStat;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;

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
}
