package foundry.alembic.stats.item.modifiers;

import com.mojang.serialization.Codec;
import foundry.alembic.stats.item.ItemModifierType;
import foundry.alembic.stats.item.ItemStat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record RemoveItemModifier(Attribute target) implements ItemModifier {
    public static final Codec<RemoveItemModifier> CODEC = BuiltInRegistries.ATTRIBUTE.byNameCodec().fieldOf("target")
            .xmap(RemoveItemModifier::new, removeItemModifier -> removeItemModifier.target)
            .codec();

    @Override
    public void compute(ItemStat.AttributeContainer container) {
        container.remove(target);
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
        return target;
    }

    @Override
    public @Nullable UUID getUUID() {
        return null;
    }
}
