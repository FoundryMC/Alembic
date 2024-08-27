package foundry.alembic.compat;

import foundry.alembic.stats.item.ItemStatManager;
import foundry.alembic.stats.item.slots.CustomSlotType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

public final class CuriosCompat {
    public static void addListeners() {
        MinecraftForge.EVENT_BUS.addListener(CuriosCompat::curiosAttributeEvent);
    }

    private static void curiosAttributeEvent(CurioAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();

        if (!ItemStatManager.hasStats(stack.getItem())) {
            return;
        }
        ItemStatManager.getStats(stack.getItem(), new CustomSlotType(event.getSlotContext().identifier()))
                .forEach(itemStat -> itemStat.computeAttributes(event.getOriginalModifiers(), event::addModifier, event::removeAttribute));
    }
}
