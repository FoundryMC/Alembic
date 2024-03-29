package foundry.alembic.client;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class TooltipHelper {
//    public static Pair<Double, Boolean> handleVanillaTooltips(AttributeModifier attributemodifier, ItemStack stack, Player player, Map.Entry<Attribute, AttributeModifier> entry, double d0, boolean flag){
//        if(player != null || !ItemStatHolder.contains(stack.getItem())) {
//            Set<UUID> uuids = ItemStatHolder.getUUIDs(stack.getItem());
//            if (uuids.contains(attributemodifier.getId())) {
//                return Pair.of(d0 + player.getAttributeBaseValue(entry.getKey()), true);
//            }
//        }
//        return Pair.of(d0, flag);
//    }

    public static double getMod(AttributeModifier mod, double d0){
        return mod.getOperation() == AttributeModifier.Operation.ADDITION ? d0 : d0 * 100d;
    }
}
