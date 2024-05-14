package foundry.alembic.util;

import foundry.alembic.attribute.UUIDSavedData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;
import java.util.function.DoubleUnaryOperator;

public class AttributeHelper {
    public static void addOrModifyModifier(LivingEntity livingEntity, Attribute attribute, ResourceLocation modifierId, DoubleUnaryOperator modifierOperator) {
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            AttributeInstance instance = livingEntity.getAttribute(attribute);
            UUIDSavedData savedData = UUIDSavedData.getOrLoad(serverLevel.getServer());
            if (instance != null) {
                modifyModifier(instance, modifierId, savedData.getOrCreate(modifierId), modifierOperator);
            }
        }
    }

    public static void modifyModifier(AttributeInstance instance, ResourceLocation modifierId, UUID modifierUuid, DoubleUnaryOperator modifierOperator) {
        AttributeModifier modifier = instance.getModifier(modifierUuid);
        if (modifier != null) {
            instance.removePermanentModifier(modifierUuid);
        }

        instance.addPermanentModifier(new AttributeModifier(modifierUuid, modifierId.toString(), modifierOperator.applyAsDouble(modifier != null ? modifier.getAmount() : 0), AttributeModifier.Operation.ADDITION));
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
