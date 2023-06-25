package foundry.alembic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import foundry.alembic.client.TooltipHelper;
import foundry.alembic.items.ItemStat;
import foundry.alembic.items.ItemStatHolder;
import foundry.alembic.items.ItemStatManager;
import foundry.alembic.items.slots.VanillaSlotType;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.antlr.v4.runtime.misc.MultiMap;
import org.antlr.v4.runtime.misc.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Alembic.MODID, value = Dist.CLIENT)
public class ForgeClientEvents {
    @SubscribeEvent
    public static void onTooltipRender(ItemTooltipEvent event) {
        if(!AlembicConfig.modifyTooltips.get()) return;
        if(!isValidItem(event.getItemStack().getItem())) return;

        int target = 0;

        Iterator<Component> iter = event.getToolTip().iterator();
        for (; iter.hasNext();) {
            Component component = iter.next();
            if(component.getString().contains("When in")) {
                target = event.getToolTip().indexOf(component) + 1;
            }
            if (shouldRemoveComponent(event.getItemStack(), component)) {
                iter.remove();
            }
        }

        if (target != 0) {
            Collection<ItemStat> stats = ItemStatManager.getStats(event.getItemStack().getItem(), new VanillaSlotType(EquipmentSlot.MAINHAND));

            if(stats.isEmpty()) return;

            Multimap<Attribute, AttributeModifier> modifiableMap = HashMultimap.create(event.getItemStack().getAttributeModifiers(EquipmentSlot.MAINHAND));

            stats.forEach(stat -> {
                stat.computeAttributes(modifiableMap, modifiableMap::put, modifiableMap::removeAll);
            });

            if (event.getEntity() == null) return;

            int finalTarget = target;
            modifiableMap.forEach((key, value) -> {
                if (isDefaultAttack(key)) return;
                double d0 = 0;
                d0 += event.getEntity().getAttributeBaseValue(key);
                d0 += event.getItemStack().getAttributeModifiers(EquipmentSlot.MAINHAND).get(key).stream().mapToDouble(AttributeModifier::getAmount).sum();
                d0 = TooltipHelper.getMod(value, d0);
                event.getToolTip().add(finalTarget, Component.literal(" ").append(Component.translatable("attribute.modifier.equals." + value.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d0), Component.translatable(key.getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
            });
        }

    }

    private static boolean isDefaultAttack(Attribute attribute) {
        return attribute == Attributes.ATTACK_DAMAGE || attribute == Attributes.ATTACK_SPEED;
    }

    private static boolean shouldRemoveComponent(ItemStack stack, Component component) {
        if (component.toString().contains("alembic")) {
            return true;
        }
        if (stack.isEnchanted() && stack.getAllEnchantments().containsKey(Enchantments.FIRE_ASPECT)) {
            return !component.toString().contains("fire_damage");
        }
        return false;
    }

    private static boolean isValidItem(Item item) {
        return item instanceof SwordItem || item instanceof TridentItem || item instanceof DiggerItem || item instanceof PotionItem || item instanceof SuspiciousStewItem;
    }
}
