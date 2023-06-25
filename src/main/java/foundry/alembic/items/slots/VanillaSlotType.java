package foundry.alembic.items.slots;

import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public record VanillaSlotType(EquipmentSlot slot) implements EquipmentSlotType {
    @Override
    public @Nullable EquipmentSlot getVanillaSlot() {
        return slot;
    }

    @Override
    public String getName() {
        return slot.getName();
    }
}
