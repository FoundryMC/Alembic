package foundry.alembic;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AlembicAPI {
    private static List<String> damageTypes = new ArrayList<>();

    public static DamageSource SOUL_FIRE = new DamageSource("soulFire");
    public static DamageSource ALCHEMICAL = new DamageSource("ALCHEMICAL");
    public static DamageSource EVOKER_FANGS = new DamageSource("evokerFangs");
    public static DamageSource GUARDIAN_BEAM = new DamageSource("guardianBeam");
    public static DamageSource ALLERGY = new DamageSource("allergy");


    public static DamageSource indirectAlchemical(Entity pSource, @Nullable Entity pIndirectEntity) {
        return (new IndirectEntityDamageSource("indirectAlchemical", pSource, pIndirectEntity)).bypassArmor().setMagic();
    }

    public static void addDefaultDamageType(String damageType) {
        damageTypes.add(damageType);
    }

    public static List<String> getDefaultDamageTypes() {
        return damageTypes;
    }
}
