package foundry.alembic.client;

import com.mojang.datafixers.util.Pair;
import foundry.alembic.items.ItemStatHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TooltipHelper {
    public static Pair<Double, Boolean> handleVanillaTooltips(AttributeModifier attributemodifier, ItemStack stack, Player player, Map.Entry<Attribute, AttributeModifier> entry, double d0, boolean flag){
        if(player != null){
            List<UUID> uuids = ItemStatHolder.getUUIDs(stack.getItem());
            if(uuids.isEmpty()) return Pair.of(d0, flag);
            if(uuids.contains(attributemodifier.getId())){
                d0 += player.getAttributeBaseValue(entry.getKey());
                flag = true;
                return Pair.of(d0, flag);
            }
        }
        return Pair.of(d0, flag);
    }

    public static void getFlag(boolean flag, List<Component> list){
        if(flag){
            System.out.println("Flag is true");
        }else{
            System.out.println("Flag is false");
        }
        int i = list.size();
    }

    public static double getMod(AttributeModifier mod, double d0){
        double d1;
        if (mod.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && mod.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
            d1 = d0;
        } else {
            d1 = d0 * 100.0D;
        }
        return d1;
    }
}
