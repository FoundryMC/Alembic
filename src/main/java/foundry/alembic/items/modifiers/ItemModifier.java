package foundry.alembic.items.modifiers;

import com.mojang.serialization.Codec;
import foundry.alembic.items.ItemModifierType;
import foundry.alembic.items.ItemStat;
import foundry.alembic.util.CodecUtil;

public sealed interface ItemModifier permits AppendItemModifier, RemoveItemModifier, ReplaceItemModifier {
    Codec<ItemModifier> DISPATCH_CODEC = CodecUtil.safeDispatch(ItemModifierType.CODEC, "type", ItemModifier::getType, ItemModifierType::getCodec);

    void compute(ItemStat.AttributeContainer container);

    ItemModifierType getType();
}
