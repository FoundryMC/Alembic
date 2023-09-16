package foundry.alembic;

import foundry.alembic.attribute.AttributeSetRegistry;
import foundry.alembic.attribute.AttributeSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Alembic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    static void onAttributeModification(final EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            for (AttributeSet attributeSet : AttributeSetRegistry.getValues()) {
                if (!event.has(type, attributeSet.getDamageAttribute())) {
                    event.add(type, attributeSet.getDamageAttribute());
                }
                if (!event.has(type, attributeSet.getShieldingAttribute())) {
                    event.add(type, attributeSet.getShieldingAttribute());
                }
                if (!event.has(type, attributeSet.getAbsorptionAttribute())) {
                    event.add(type, attributeSet.getAbsorptionAttribute());
                }
                if (!event.has(type, attributeSet.getResistanceAttribute())) {
                    event.add(type, attributeSet.getResistanceAttribute());
                }
            }
        }
    }

    static void registerBrewingRecipes(final FMLCommonSetupEvent event) {

    }
}
