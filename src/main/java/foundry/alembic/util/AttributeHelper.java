package foundry.alembic.util;

import foundry.alembic.attribute.UUIDManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class AttributeHelper {
    public static void addOrModifyModifier(AttributeInstance instance, ResourceLocation modifierId, double bonus) {
        addOrModifyModifier(instance, modifierId, UUIDManager.getInstance().getOrCreate(modifierId), bonus);
    }

    public static void addOrModifyModifier(AttributeInstance instance, ResourceLocation modifierId, UUID modifierUuid, double bonus) {
        if (instance.getModifier(modifierUuid) == null) {
            instance.addPermanentModifier(new AttributeModifier(modifierUuid, modifierId.toString(), bonus, AttributeModifier.Operation.ADDITION));
        } else {
            AttributeModifier modifier = instance.getModifier(modifierUuid);
            instance.removePermanentModifier(modifierUuid);
            instance.addPermanentModifier(new AttributeModifier(modifierUuid, modifierId.toString(), modifier.getAmount()+bonus, AttributeModifier.Operation.ADDITION));
        }
    }
}
