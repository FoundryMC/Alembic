package foundry.alembic;

import foundry.alembic.client.AlembicOverlayRegistry;
import foundry.alembic.particle.AlembicParticleRegistry;
import foundry.alembic.particle.AlembicParticleType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AlembicClient {
    static void init(IEventBus modBus) {
        AlembicOverlayRegistry.init();
        modBus.addListener(AlembicClient::onRegisterParticles);
    }

    private static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        AlembicParticleRegistry.PARTICLES.forEach((id, particle) -> {
            event.registerSpriteSet(particle.get(), AlembicParticleType.DamageIndicatorProvider::new);
        });
    }
}
