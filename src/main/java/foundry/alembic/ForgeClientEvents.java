package foundry.alembic;

import com.mojang.datafixers.util.Pair;
import foundry.alembic.client.TooltipHelper;
import foundry.alembic.items.ItemStatHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
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
    public static void onTooltipRender(ItemTooltipEvent event){
        int target = 0;
        List<Component> toRemove = new ArrayList<>();
        for(Component component : event.getToolTip()){
            if(component.getString().contains("When in")){
                target = event.getToolTip().indexOf(component) + 1;
            }
            if(event.getItemStack().isEnchanted()){
                if(event.getItemStack().getAllEnchantments().containsKey(Enchantments.FIRE_ASPECT)){
                    if((component.toString().contains("alembic") &&!component.toString().contains("fire_damage"))){
                        toRemove.add(component);
                    }
                } else {
                    if((component.toString().contains("alembic"))){
                        toRemove.add(component);
                    }
                }
            } else if ((component.toString().contains("alembic"))
                    && (event.getItemStack().getItem() instanceof SwordItem || event.getItemStack().getItem() instanceof TridentItem || event.getItemStack().getItem() instanceof DiggerItem)){
                toRemove.add(component);
            }
        }
        event.getToolTip().removeAll(toRemove);
        if(target != 0){
            int finalTarget = target;
            List<Pair<Attribute, AttributeModifier>> holder = ItemStatHolder.get(event.getItemStack().getItem());
            if(holder == null) return;
            holder.forEach(pair -> {
                if(event.getEntity() == null) return;
                if(pair.getFirst().descriptionId.contains("physical_damage")) return;
                double d0 = pair.getSecond().getAmount();
                d0 += event.getEntity().getAttributeBaseValue(pair.getFirst());
                d0 = TooltipHelper.getMod(pair.getSecond(), d0);
                event.getToolTip().add(finalTarget, Component.literal(" ").append(Component.translatable("attribute.modifier.equals." + pair.getSecond().getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d0), Component.translatable(pair.getFirst().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
            });
        }

    }
}
