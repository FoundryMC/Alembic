package foundry.alembic.event;

import foundry.alembic.types.AlembicDamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class AlembicDamageEvent extends Event {
    private LivingEntity target;
    private LivingEntity attacker;
    private AlembicDamageType damageType;
    private float damage;
    private float resistance;

    public AlembicDamageEvent(LivingEntity target, LivingEntity attacker, AlembicDamageType damageType, float damage, float resistance) {
        this.target = target;
        this.attacker = attacker;
        this.damageType = damageType;
        this.damage = damage;
        this.resistance = resistance;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public AlembicDamageType getDamageType() {
        return damageType;
    }

    public float getDamage() {
        return damage;
    }

    public float getResistance() {
        return resistance;
    }

    public static class Pre extends AlembicDamageEvent {
        public Pre(LivingEntity target, LivingEntity attacker, AlembicDamageType damageType, float damage, float resistance) {
            super(target, attacker, damageType, damage, resistance);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    public static class Post extends AlembicDamageEvent {
        public Post(LivingEntity target, LivingEntity attacker, AlembicDamageType damageType, float damage, float resistance) {
            super(target, attacker, damageType, damage, resistance);
        }

        @Override
        public boolean isCancelable() {
            return false;
        }
    }
}
