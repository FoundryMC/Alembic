package foundry.alembic.types.potion;

import foundry.alembic.mixin.MobEffectInstanceAccessor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;

public class OverrideHelper {
    public static void addFireEffect(int ticks, LivingEntity le, String block) {
        if(le.level.isClientSide) return;
        MobEffect eff = le.getFeetBlockState().is(Blocks.SOUL_FIRE) || block.equals("soul") ? AlembicPotionRegistry.SOUL_FIRE.get() : AlembicPotionRegistry.FIRE.get();
        if (le.hasEffect(eff)) {
            MobEffectInstance effect = le.getEffect(eff);
            if (effect == null) return;
            ((MobEffectInstanceAccessor) effect).setDuration(ticks);
            le.removeEffect(eff);
            le.addEffect(new MobEffectInstance(eff, ticks+2, 0, true, false));
        } else {
            le.addEffect(new MobEffectInstance(eff, ticks+2, 0, true, false));
        }
    }

    public static void removeFireEffect(LivingEntity le){
        if(le.level.isClientSide) return;
        if(le.hasEffect(AlembicPotionRegistry.FIRE.get())){
            ((AlembicFlammablePlayer)le).setAlembicLastFireBlock("");
            MobEffectInstance effect = le.getEffect(AlembicPotionRegistry.FIRE.get());
            if(effect == null) return;
            ((MobEffectInstanceAccessor)effect).setDuration(0);
        }
        if(le.hasEffect(AlembicPotionRegistry.SOUL_FIRE.get())){
            ((AlembicFlammablePlayer)le).setAlembicLastFireBlock("");
            MobEffectInstance effect = le.getEffect(AlembicPotionRegistry.SOUL_FIRE.get());
            if(effect == null) return;
            ((MobEffectInstanceAccessor)effect).setDuration(0);
        }
    }
}
