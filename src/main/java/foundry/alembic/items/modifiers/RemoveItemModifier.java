package foundry.alembic.items.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.items.ItemModifier;
import foundry.alembic.items.ItemModifierType;
import foundry.alembic.items.ItemStat;
import foundry.alembic.items.ModifierApplication;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class RemoveItemModifier implements ItemModifier {
    public static final Codec<RemoveItemModifier> CODEC = Registry.ATTRIBUTE.byNameCodec().fieldOf("attribute")
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
