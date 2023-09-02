package foundry.alembic.items;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.util.CodecUtil;

public abstract class ItemModifier {
    public static final Codec<ItemModifier> DISPATCH_CODEC = CodecUtil.safeDispatch(ItemModifierType.CODEC, "type", ItemModifier::getType, ItemModifierType::getCodec);
    public static <T extends ItemModifier> Products.P1<RecordCodecBuilder.Mu<T>, ModifierApplication> base(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                ModifierApplication.CODEC.optionalFieldOf("application", ModifierApplication.INSTANT).forGetter(ItemModifier::getApplication)
        );
    }

    private final ModifierApplication application;

    public ItemModifier(ModifierApplication application) {
        this.application = application;
    }

    public ModifierApplication getApplication() {
        return application;
    }

    public abstract void compute(ItemStat.AttributeContainer container);

    public abstract ItemModifierType getType();
}
