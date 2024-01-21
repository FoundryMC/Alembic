package foundry.alembic.util;

import foundry.alembic.attribute.UUIDFactory;
import foundry.alembic.attribute.UUIDManager;
import foundry.alembic.attribute.UUIDSavedData;
import foundry.alembic.stats.item.slots.EquipmentSlotType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class AttributeHelper {
    public static void addOrModifyModifier(LivingEntity livingEntity, Attribute attribute, ResourceLocation modifierId, double bonus) {
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            AttributeInstance instance = livingEntity.getAttribute(attribute);
            if (instance != null) {
                addOrModifyModifier(instance, modifierId, UUIDFactory.getOrCreate(serverLevel, modifierId), bonus);
            }
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

//    public static UUID attributeUuid(Level level, Attribute attribute, InteractionHand hand) {
//        ResourceLocation attributeId = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
//        if (hand == InteractionHand.MAIN_HAND) {
//            return UUIDFactory.getOrCreate(level, attributeId);
//        } else {
//            return UUIDFactory.getOrCreate(level, attributeId.withSuffix("_offhand"));
//        }
//    }
//
//    public static UUID baseAttributeUuid(Level level, Attribute attribute) {
//        return attributeUuid(level, attribute, InteractionHand.MAIN_HAND);
//    }

    public static UUID baseAttributeUuid(Attribute attribute) {
        return UUIDManager.getOrCreate(BuiltInRegistries.ATTRIBUTE.getKey(attribute));
    }

    public static UUID slotUuid(EquipmentSlotType slotType) {
        return UUIDManager.getOrCreate(new ResourceLocation(slotType.getName()));
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
