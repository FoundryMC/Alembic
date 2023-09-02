package foundry.alembic.items.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.items.ItemModifier;
import foundry.alembic.items.ItemModifierType;
import foundry.alembic.items.ItemStat;
import foundry.alembic.items.ModifierApplication;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class RemoveItemModifier extends ItemModifier {
    public static final Codec<RemoveItemModifier> CODEC = RecordCodecBuilder.create(instance ->
            base(instance).and(
                    Registry.ATTRIBUTE.byNameCodec().fieldOf("attribute").forGetter(removeItemModifier -> removeItemModifier.attribute)
            ).apply(instance, RemoveItemModifier::new)
    );

    private final Attribute attribute;

    public RemoveItemModifier(ModifierApplication application, Attribute attribute) {
        super(application);
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
