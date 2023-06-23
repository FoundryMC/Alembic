package foundry.alembic;

import foundry.alembic.attribute.AttributeRegistry;
import foundry.alembic.attribute.AttributeSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Alembic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    static void onAttributeModification(final EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            for (AttributeSet attributeSet : AttributeRegistry.ID_TO_SET_BIMAP.values()) {
                event.add(type, attributeSet.getBaseAttribute());
                attributeSet.getShieldingAttribute().ifPresent(attribute -> event.add(type, attribute, 0));
                attributeSet.getResistanceAttribute().ifPresent(attribute -> event.add(type, attribute, 1));
                attributeSet.getAbsorptionAttribute().ifPresent(attribute -> event.add(type, attribute, 0));
            }
        }
    }
}
