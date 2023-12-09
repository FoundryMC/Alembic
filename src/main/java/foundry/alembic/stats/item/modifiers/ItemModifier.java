package foundry.alembic.stats.item.modifiers;

import com.mojang.serialization.Codec;
import foundry.alembic.stats.item.ItemModifierType;
import foundry.alembic.stats.item.ItemStat;
import foundry.alembic.codecs.CodecUtil;

public sealed interface ItemModifier permits AppendItemModifier, RemoveItemModifier, ReplaceItemModifier {
    Codec<ItemModifier> DISPATCH_CODEC = CodecUtil.safeDispatch(ItemModifierType.CODEC, "type", ItemModifier::getType, ItemModifierType::getCodec);

    void compute(ItemStat.AttributeContainer container);

    ItemModifierType getType();

    @Nullable Attribute getAttribute();

    @Nullable Attribute getTarget();

    @Nullable UUID getUUID();
}
