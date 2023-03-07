package foundry.alembic;

import foundry.alembic.event.AlembicDamageEvent;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AlembicAPI {
    private static final List<String> DAMAGE_TYPES = new ArrayList<>();

    public static DamageSource SOUL_FIRE = new DamageSource("soulFire");
    public static DamageSource ALCHEMICAL = new DamageSource("ALCHEMICAL");
    public static DamageSource EVOKER_FANGS = new DamageSource("evokerFangs");
    public static DamageSource GUARDIAN_BEAM = new DamageSource("guardianBeam");
    public static DamageSource ALLERGY = new DamageSource("allergy");


    public static DamageSource indirectAlchemical(Entity pSource, @Nullable Entity pIndirectEntity) {
        return (new IndirectEntityDamageSource("indirectAlchemical", pSource, pIndirectEntity)).bypassArmor().setMagic();
    }

    public static void addDefaultDamageType(String damageType) {
        DAMAGE_TYPES.add(damageType);
    }

    public static List<String> getDefaultDamageTypes() {
        return DAMAGE_TYPES;
    }

    public static List<String> getDefaultPotionEffects() {
        return List.of("fire_damage", "arcane_damage", "alchemical_damage");
    }

    public static AlembicDamageType getDamageType(String damageType) {
        return DamageTypeRegistry.getDamageType(damageType);
    }

    public static AlembicDamageType getDamageType(DamageSource damageSource) {
        return DamageTypeRegistry.getDamageType(damageSource);
    }

    public static float activatePreEvent(LivingEntity target, LivingEntity attacker, AlembicDamageType damageType, float damage, float resistance) {
        AlembicDamageEvent.Pre event = new AlembicDamageEvent.Pre(target, attacker, damageType, damage, resistance);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getDamage();
    }

    public static float activatePostEvent(LivingEntity target, LivingEntity attacker, AlembicDamageType damageType, float damage, float resistance) {
        AlembicDamageEvent.Post event = new AlembicDamageEvent.Post(target, attacker, damageType, damage, resistance);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getDamage();
    }
}
