package foundry.alembic.items;

import com.mojang.serialization.Codec;
import foundry.alembic.util.CodecUtil;

public interface ItemModifier {
    Codec<ItemModifier> DISPATCH_CODEC = CodecUtil.safeDispatch(ItemModifierType.CODEC, "type", ItemModifier::getType, ItemModifierType::getCodec);

    void compute(ItemStat.AttributeContainer container);

    ItemModifierType getType();
}
