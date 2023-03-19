package foundry.alembic.items;

import net.minecraft.world.item.Item;

import java.util.UUID;

public class ItemUUIDAccess extends Item {
    public ItemUUIDAccess(Properties pProperties) {
        super(pProperties);
    }

    public static UUID getbaseAttackDamageUUID() {
        return BASE_ATTACK_DAMAGE_UUID;
    }

    public static UUID getbaseAttackSpeedUUID() {
        return BASE_ATTACK_SPEED_UUID;
    }
}
