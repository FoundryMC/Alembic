package foundry.alembic.items.slots;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import foundry.alembic.util.CodecUtil;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public interface EquipmentSlotType {
    Codec<EquipmentSlotType> CODEC = Codec.either(CodecUtil.EQUIPMENT_SLOT_CODEC, Codec.STRING).xmap(
            either -> either.map(VanillaSlotType::new, CustomSlotType::new),
            equipmentSlotType -> equipmentSlotType.getVanillaSlot() != null ? Either.left(equipmentSlotType.getVanillaSlot()) : Either.right(equipmentSlotType.getName())
    );

    @Nullable
    EquipmentSlot getVanillaSlot();

    String getName();
}
