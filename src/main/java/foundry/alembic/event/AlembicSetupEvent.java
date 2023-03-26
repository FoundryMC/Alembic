package foundry.alembic.event;

import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class AlembicSetupEvent extends Event {
    List<String> damageTypes = new ArrayList<>();
    List<String> potionEffects = new ArrayList<>();
    List<String> particles = new ArrayList<>();

    public AlembicSetupEvent() {
    }

    public List<String> getDamageTypes() {
        return damageTypes;
    }

    public List<String> getPotionEffects() {
        return potionEffects;
    }

    public List<String> getParticles() {
        return particles;
    }

    public void addDamageType(String damageType) {
        damageTypes.add(damageType);
    }

    public void addPotionEffect(String potionEffect) {
        potionEffects.add(potionEffect);
    }

    public void addParticle(String particle) {
        particles.add(particle);
    }
}
