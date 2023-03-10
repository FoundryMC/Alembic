package foundry.alembic;

import foundry.alembic.particle.AlembicParticleRegistry;
import foundry.alembic.particle.AlembicParticleType;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Alembic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    static void onAttributeModification(final EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            for (AlembicDamageType damageType : DamageTypeRegistry.getDamageTypes()) {
                event.add(type, damageType.getAttribute(), damageType.getBase());
                event.add(type, damageType.getShieldAttribute(), 0);
                event.add(type, damageType.getResistanceAttribute(), 1);
                event.add(type, damageType.getAbsorptionAttribute(), 0);
            }
        }
    }

    @SubscribeEvent
    static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        AlembicParticleRegistry.PARTICLES.forEach((id, particle) -> {
            event.register(particle.get(), AlembicParticleType.DamageIndicatorProvider::new);
        });
    }

}
