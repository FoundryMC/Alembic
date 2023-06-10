package foundry.alembic.types.tag.condition.predicates;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemPredicate {
    public static final ItemPredicate EMPTY = new ItemPredicate(Items.AIR);

    public static final Codec<ItemPredicate> CODEC = ForgeRegistries.ITEMS.getCodec().xmap(
            ItemPredicate::new,
            itemPredicate -> itemPredicate.item
    );

    private final Item item;

    public ItemPredicate(Item item) {
        this.item = item;
    }

    public boolean matches(ItemStack itemStack) {
        if (this == EMPTY) {
            return true;
        }
        return itemStack.is(item);
    }
}
