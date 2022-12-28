package foundry.alembic;

import foundry.alembic.types.AlembicDamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class AlembicDamageHelper {

    public float getDamageAfterResistance(LivingEntity target, MobEffect effect, AlembicDamageType type, float damage){
        if(effect == MobEffects.DAMAGE_RESISTANCE){
            int i = (target.getEffect(effect).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f = damage * (float)j;
            damage = Math.max(f / 25.0F, 0.0F);
            return damage;
        } else {
            int i = (target.getEffect(effect).getAmplifier() + 1);
            return damage * (1.0F - (float)i * 0.1F);
        }
    }
}
