package foundry.alembic;

import foundry.alembic.attribute.AttributeSetRegistry;
import foundry.alembic.attribute.AttributeSet;
import foundry.alembic.potion.AlembicPotionDataHolder;
import foundry.alembic.potion.AlembicPotionRecipe;
import foundry.alembic.potion.PotionModifier;
import foundry.alembic.types.AlembicTypeModifier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Alembic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    static void onAttributeModification(final EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            for (AttributeSet attributeSet : AttributeSetRegistry.getValues()) {
                if (!event.has(type, attributeSet.getDamageAttribute())) {
                    event.add(type, attributeSet.getDamageAttribute());
                }
                if (!event.has(type, attributeSet.getShieldingAttribute())) {
                    event.add(type, attributeSet.getShieldingAttribute());
                }
                if (!event.has(type, attributeSet.getAbsorptionAttribute())) {
                    event.add(type, attributeSet.getAbsorptionAttribute());
                }
                if (!event.has(type, attributeSet.getResistanceAttribute())) {
                    event.add(type, attributeSet.getResistanceAttribute());
                }
            }
        }
    }

    @SubscribeEvent
    static void registerBrewingRecipes(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (AttributeSet set : AttributeSetRegistry.getValues()) {
                ResourceLocation setId = AttributeSetRegistry.getId(set);
                Optional<AlembicPotionDataHolder> dataHolderOptional = set.getPotionDataHolder();
                dataHolderOptional.ifPresent(alembicPotionDataHolder -> {
                    if (dataHolderOptional.get().getRecipe() != AlembicPotionRecipe.EMPTY) {
                        AlembicPotionDataHolder dataHolder = dataHolderOptional.get();
                        AlembicPotionRecipe recipe = dataHolder.getRecipe();
                        Ingredient baseIngredient = recipe.base();
                        Ingredient reagentIngredient = recipe.reagent();
                        Potion baseResistancePotion = Registry.POTION.get(AlembicTypeModifier.RESISTANCE.computePotionId(setId));
                        Potion longResistancePotion = Registry.POTION.get(AlembicTypeModifier.RESISTANCE.computePotionId(setId, PotionModifier.LONG));
                        Potion strongResistancePotion = Registry.POTION.get(AlembicTypeModifier.RESISTANCE.computePotionId(setId, PotionModifier.STRONG));

                        BrewingRecipeRegistry.addRecipe(baseIngredient, reagentIngredient, PotionUtils.setPotion(new ItemStack(Items.POTION), baseResistancePotion));
                        addPotionRecipe(baseResistancePotion, Items.GLOWSTONE_DUST, longResistancePotion);
                        if (dataHolder.getMaxStrengthLevel() > 1) {
                            addPotionRecipe(baseResistancePotion, Items.REDSTONE, strongResistancePotion);
                            Potion lastStrongPotion = strongResistancePotion;
                            for (int i = 2; i < dataHolder.getMaxStrengthLevel(); i++) {
                                ResourceLocation strongId = AlembicTypeModifier.RESISTANCE.computePotionId(setId, PotionModifier.STRONG);
                                Potion strongerResistancePotion = Registry.POTION.get(new ResourceLocation(strongId.getNamespace(), strongId.getPath() + "_" + i));
                                addPotionRecipe(lastStrongPotion, Items.REDSTONE, strongerResistancePotion);
                                lastStrongPotion = strongerResistancePotion;
                            }
                        }
                    }
                });
            }
        });
    }

    private static void addPotionRecipe(Potion input, Item reagent, Potion output) {
        BrewingRecipeRegistry.addRecipe(StrictNBTIngredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), input)), Ingredient.of(reagent), PotionUtils.setPotion(new ItemStack(Items.POTION), output));
    }
}
