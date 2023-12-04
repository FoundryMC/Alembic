package foundry.alembic.stats.item;

import com.mojang.serialization.Codec;
import foundry.alembic.stats.item.modifiers.AppendItemModifier;
import foundry.alembic.stats.item.modifiers.ItemModifier;
import foundry.alembic.stats.item.modifiers.RemoveItemModifier;
import foundry.alembic.stats.item.modifiers.ReplaceItemModifier;
import net.minecraft.util.StringRepresentable;

import java.util.function.Supplier;

public enum ItemModifierType implements StringRepresentable {
    APPEND("append", () -> AppendItemModifier.CODEC),
    REPLACE("replace", () -> ReplaceItemModifier.CODEC),
    REMOVE("remove", () -> RemoveItemModifier.CODEC);

    public static final Codec<ItemModifierType> CODEC = StringRepresentable.fromEnum(ItemModifierType::values);

    private final String name;
    private final Supplier<Codec<? extends ItemModifier>> codec;

    ItemModifierType(String name, Supplier<Codec<? extends ItemModifier>> codec) {
        this.name = name;
        this.codec = codec;
    }

    public Codec<? extends ItemModifier> getCodec() {
        return codec.get();
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
