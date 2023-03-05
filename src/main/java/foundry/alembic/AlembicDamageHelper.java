package foundry.alembic;

import foundry.alembic.types.AlembicDamageType;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class AlembicDamageHelper {

    public static float getDamageAfterResistance(LivingEntity target, MobEffect effect, AlembicDamageType type, float damage){
        if(effect == MobEffects.DAMAGE_RESISTANCE){
            int i = (target.getEffect(effect).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f = damage * (float)j;
            damage = Math.max(f / 25.0F, 0.0F);
            float f2 = f - damage;
            if(f2 > 0.0F && f2 < 3.4028235E37F){
                if(target instanceof Player){
                    ((Player)target).awardStat(Stats.DAMAGE_RESISTED, Math.round(f2 * 10.0F));
                }
            }
            return damage;
        } else {
            int i = (target.getEffect(effect).getAmplifier() + 1);
            return damage * (1.0F - (float)i * 0.1F);
        }
    }

    public static float getDamageAfterAttributeAbsorb(LivingEntity target, AlembicDamageType type, float damage){
        if(target.hasEffect(MobEffects.DAMAGE_RESISTANCE) && type.getId().equals(Alembic.location("physical_damage"))){
            damage = getDamageAfterResistance(target, MobEffects.DAMAGE_RESISTANCE, type, damage);
        }
        return damage;
    }
}
