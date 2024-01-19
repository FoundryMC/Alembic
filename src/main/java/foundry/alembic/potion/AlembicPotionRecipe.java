package foundry.alembic.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.codecs.CodecUtil;
import net.minecraft.world.item.crafting.Ingredient;

public record AlembicPotionRecipe(Ingredient reagent, Ingredient base) {
    public static final AlembicPotionRecipe EMPTY = new AlembicPotionRecipe(Ingredient.EMPTY, Ingredient.EMPTY);
    public static final Codec<AlembicPotionRecipe> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.INGREDIENT_FROM_EITHER.fieldOf("reagent").forGetter(AlembicPotionRecipe::reagent),
                    CodecUtil.INGREDIENT_FROM_EITHER.fieldOf("base").forGetter(AlembicPotionRecipe::base)
        ).apply(instance, AlembicPotionRecipe::new)
    );
}
