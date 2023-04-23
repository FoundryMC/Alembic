package foundry.alembic;

import foundry.alembic.attribute.AttributeRegistry;
import foundry.alembic.attribute.AttributeSet;
import foundry.alembic.particle.AlembicParticleRegistry;
import foundry.alembic.particle.AlembicParticleType;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Alembic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    static void onAttributeModification(final EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            for (AttributeSet attributeSet : AttributeRegistry.ID_TO_SET_BIMAP.values()) {
                event.add(type, attributeSet.getBaseAttribute(), attributeSet.getBaseAttribute().defaultValue);
                attributeSet.getShieldingAttribute().ifPresent(attribute -> event.add(type, attribute, 0));
                attributeSet.getResistanceAttribute().ifPresent(attribute -> event.add(type, attribute, 1));
                attributeSet.getAbsorptionAttribute().ifPresent(attribute -> event.add(type, attribute, 0));
            }
        }
    }

    @SubscribeEvent
    static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        AlembicParticleRegistry.PARTICLES.forEach((id, particle) -> {
            event.register(particle.get(), AlembicParticleType.DamageIndicatorProvider::new);
        });
    }

    @SubscribeEvent
    static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {

        });
    }
}
