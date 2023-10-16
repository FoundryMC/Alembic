package foundry.alembic.util;

import foundry.alembic.attribute.UUIDSavedData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class AttributeHelper {
    public static void addOrModifyModifier(LivingEntity livingEntity, Attribute attribute, ResourceLocation modifierId, double bonus) {
        if (livingEntity.getLevel() instanceof ServerLevel serverLevel) {
            AttributeInstance instance = livingEntity.getAttribute(attribute);
            UUIDSavedData savedData = UUIDSavedData.getOrLoad(serverLevel.getServer());
            addOrModifyModifier(instance, modifierId, savedData.getOrCreate(modifierId), bonus);
        }
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

//    public static double applyModifiersTo(AttributeInstance instance, double num) {
//        for(AttributeModifier attributemodifier : instance.getModifiers(AttributeModifier.Operation.ADDITION)) {
//            num += attributemodifier.getAmount();
//        }
//
//        double d1 = d0;
//
//        for(AttributeModifier attributemodifier1 : instance.getModifiers(AttributeModifier.Operation.MULTIPLY_BASE)) {
//            d1 += d0 * attributemodifier1.getAmount();
//        }
//
//        for(AttributeModifier attributemodifier2 : instance.getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
//            d1 *= 1.0D + attributemodifier2.getAmount();
//        }
//
//        return instance.getAttribute().sanitizeValue(d1);
//    }
}
