package foundry.alembic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import foundry.alembic.attribute.AttributeSetRegistry;
import foundry.alembic.client.TooltipHelper;
import foundry.alembic.stats.item.ItemStat;
import foundry.alembic.stats.item.ItemStatManager;
import foundry.alembic.stats.item.slots.VanillaSlotType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Alembic.MODID, value = Dist.CLIENT)
public class ForgeClientEvents {
    @SubscribeEvent
    public static void onTooltipRender(ItemTooltipEvent event) {
        if(!AlembicConfig.modifyTooltips.get()) return;
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        if(!isValidItem(stack.getItem())) {
            // TODO: AttributeSetRegistry needs to be the same on both client and server, so we should probably do some kind of networking for that.
            if (isVanillaFireResistancePotion(stack) && AttributeSetRegistry.exists(Alembic.location("fire_damage"))) {
                Attribute attribute = AttributeSetRegistry.getValue(Alembic.location("fire_damage")).getResistanceAttribute();
                tooltip.add(CommonComponents.EMPTY);
                tooltip.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
                tooltip.add(Component.translatable("attribute.modifier.plus." + AttributeModifier.Operation.MULTIPLY_TOTAL.toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(10), Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
            }
            return;
        }
        if (event.getEntity() == null) return;
        Player player = event.getEntity();

        int target = 0;

        Iterator<Component> iter = event.getToolTip().iterator();
        while (iter.hasNext()) {
            Component component = iter.next();
            if(component.getString().contains("When in")) {
                target = event.getToolTip().indexOf(component) + 1;
            }
            if (shouldRemoveComponent(stack, component)) {
                iter.remove();
            }
        }

        if (target != 0) {
            Collection<ItemStat> stats = ItemStatManager.getStats(stack.getItem(), new VanillaSlotType(EquipmentSlot.MAINHAND));

            if(stats.isEmpty()) return;

            Multimap<Attribute, AttributeModifier> modifiableMap = HashMultimap.create(stack.getAttributeModifiers(EquipmentSlot.MAINHAND));

            stats.forEach(stat -> {
                stat.computeAttributes(modifiableMap, modifiableMap::put, modifiableMap::removeAll);
            });

            int finalTarget = target;
            modifiableMap.forEach((key, value) -> {
                if (isDefaultAttack(key)) return;
                double d0 = 0;
                d0 += player.getAttributeBaseValue(key);
                d0 += stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(key).stream().mapToDouble(AttributeModifier::getAmount).sum();
                d0 = TooltipHelper.getMod(value, d0);
                tooltip.add(finalTarget, Component.literal(" ").append(Component.translatable("attribute.modifier.equals." + value.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d0), Component.translatable(key.getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
            });
        }

    }

    private static boolean isDefaultAttack(Attribute attribute) {
        return attribute == Attributes.ATTACK_DAMAGE || attribute == Attributes.ATTACK_SPEED;
    }

    private static boolean shouldRemoveComponent(ItemStack stack, Component component) {
        if (!component.toString().contains("alembic")) {
            return false;
        }
        if (stack.isEnchanted()) {
            if (stack.getAllEnchantments().containsKey(Enchantments.FIRE_ASPECT)) {
                return !component.toString().contains("fire_damage");
            }
            return true;
        }

        return true;
    }

    private static boolean isValidItem(Item item) {
        return item instanceof SwordItem || item instanceof TridentItem || item instanceof DiggerItem;
    }

    private static boolean isVanillaFireResistancePotion(ItemStack stack) {
        Potion potion;
        return (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) && ((potion = PotionUtils.getPotion(stack)) == Potions.FIRE_RESISTANCE || potion == Potions.LONG_FIRE_RESISTANCE);
    }
}
