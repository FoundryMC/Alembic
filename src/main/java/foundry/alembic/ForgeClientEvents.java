package foundry.alembic;

import foundry.alembic.client.TooltipHelper;
import foundry.alembic.items.ItemStat;
import foundry.alembic.items.ItemStatHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Alembic.MODID, value = Dist.CLIENT)
public class ForgeClientEvents {
    @SubscribeEvent
    public static void onTooltipRender(ItemTooltipEvent event) {
        if(!AlembicConfig.modifyTooltips.get()) return;
        if(event.getItemStack().getItem() instanceof PotionItem || event.getItemStack().getItem() instanceof SuspiciousStewItem) return;
        int target = 0;
        List<Component> toRemove = new ArrayList<>();
        if(!isValidItem(event.getItemStack().getItem())) return;

        for(Component component : event.getToolTip()){
            if(component.getString().contains("When in")) {
                target = event.getToolTip().indexOf(component) + 1;
                removeAttributeTooltip(event.getItemStack(), toRemove, component);
            }
            removeAttributeTooltip(event.getItemStack(), toRemove, component);
        }

        event.getToolTip().removeAll(toRemove);

        if(target != 0){
            int finalTarget = target;
            ItemStat stat = ItemStatHolder.get(event.getItemStack().getItem());

            if(stat == null) return;

            stat.createAttributes().forEach((key, value) -> {
                if (event.getEntity() == null) return;
                if (key.descriptionId.contains("physical_damage")) return;
                double d0 = 0;
                d0 += event.getEntity().getAttributeBaseValue(key);
                d0 += event.getItemStack().getAttributeModifiers(EquipmentSlot.MAINHAND).get(key).stream().mapToDouble(AttributeModifier::getAmount).sum();
                d0 = TooltipHelper.getMod(value, d0);
                event.getToolTip().add(finalTarget, Component.literal(" ").append(Component.translatable("attribute.modifier.equals." + value.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d0), Component.translatable(key.getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
            });
        }

    }

    private static void removeAttributeTooltip(ItemStack stack, List<Component> toRemove, Component component) {
        if (stack.isEnchanted()) {
            if (stack.getAllEnchantments().containsKey(Enchantments.FIRE_ASPECT)) {
                if (component.toString().contains("alembic") && !component.toString().contains("fire_damage")) {
                    toRemove.add(component);
                }
            } else {
                if (component.toString().contains("alembic")) {
                    toRemove.add(component);
                }
            }
        } else if (component.toString().contains("alembic")) {
            toRemove.add(component);
        }
    }

    private static boolean isValidItem(Item item) {
        return item instanceof SwordItem || item instanceof TridentItem || item instanceof DiggerItem;
    }
}
