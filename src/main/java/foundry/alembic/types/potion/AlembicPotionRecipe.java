package foundry.alembic.types.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.Alembic;
import net.minecraft.world.item.ItemStack;

public class AlembicPotionRecipe {
    public static Codec<AlembicPotionRecipe> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ItemStack.CODEC.fieldOf("reagent").forGetter(AlembicPotionRecipe::getReagent),
                ItemStack.CODEC.fieldOf("base").forGetter(AlembicPotionRecipe::getBase)
        ).apply(instance, (reagent, base) -> {
                Alembic.LOGGER.error("reagent: " + reagent);
                Alembic.LOGGER.error("base: " + base);
            return new AlembicPotionRecipe(reagent, base);
            })
    );
    private ItemStack reagent;
    private ItemStack base;

    public AlembicPotionRecipe(ItemStack reagent, ItemStack base){
        this.reagent = reagent;
        this.base = base;
    }

    public ItemStack getReagent(){
        return reagent;
    }

    public ItemStack getBase(){
        return base;
    }
}
