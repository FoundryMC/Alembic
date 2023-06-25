package foundry.alembic.items.modifiers;

import com.mojang.serialization.Codec;
import foundry.alembic.items.ItemModifier;
import foundry.alembic.items.ItemModifierType;
import foundry.alembic.items.ItemStat;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attribute;

public record RemoveItemModifier(Attribute attribute) implements ItemModifier {
    public static final Codec<RemoveItemModifier> CODEC = Registry.ATTRIBUTE.byNameCodec().fieldOf("attribute").xmap(
            RemoveItemModifier::new,
            RemoveItemModifier::attribute
    ).codec();


    @Override
    public void compute(ItemStat.AttributeContainer container) {
        container.remove(attribute);
    }

    @Override
    public ItemModifierType getType() {
        return ItemModifierType.REMOVE;
    }
}
