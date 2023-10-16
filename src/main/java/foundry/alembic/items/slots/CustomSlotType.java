package foundry.alembic.items.slots;

import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public record CustomSlotType(String customName) implements EquipmentSlotType {
    @Override
    public @Nullable EquipmentSlot getVanillaSlot() {
        return null;
    }

    @Override
    public String getName() {
        return customName;
    }
}
