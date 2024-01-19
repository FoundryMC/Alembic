package foundry.alembic.types.tag.condition.predicates;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemPredicate {
    public static final ItemPredicate EMPTY = new ItemPredicate(TagOrElementPredicate.alwaysTrue());

    public static final Codec<ItemPredicate> CODEC = TagOrElementPredicate.codec(Registries.ITEM, BuiltInRegistries.ITEM::getOptional, (item1, itemTagKey) -> item1.builtInRegistryHolder().is(itemTagKey)).xmap(
            ItemPredicate::new,
            itemPredicate -> itemPredicate.item
    );

    private final TagOrElementPredicate<Item> item;

    public ItemPredicate(TagOrElementPredicate<Item> item) {
        this.item = item;
    }

    public boolean matches(ItemStack itemStack) {
        if (this == EMPTY) {
            return true;
        }
        return item.matches(itemStack.getItem());
    }
}
