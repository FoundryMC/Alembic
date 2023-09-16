package foundry.alembic.items;

import com.mojang.serialization.Codec;
import foundry.alembic.util.CodecUtil;

public interface ItemModifier {
    public static final Codec<ItemModifier> DISPATCH_CODEC = CodecUtil.safeDispatch(ItemModifierType.CODEC, "type", ItemModifier::getType, ItemModifierType::getCodec);

    public abstract void compute(ItemStat.AttributeContainer container);

    public abstract ItemModifierType getType();
}
