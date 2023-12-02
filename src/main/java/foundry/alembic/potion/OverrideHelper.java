package foundry.alembic.potion;

import foundry.alembic.caps.AlembicFlammable;
import foundry.alembic.caps.AlembicFlammableHandler;
import foundry.alembic.mixin.MobEffectInstanceAccessor;
import foundry.alembic.mobeffect.AlembicMobEffectRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;

public class OverrideHelper {
    public static void addFireEffect(int ticks, LivingEntity le) {
        if(le.level().isClientSide) return;
        String s = le.getCapability(AlembicFlammableHandler.CAPABILITY, null).map(AlembicFlammable::getFireType).orElse("normal");
        MobEffect eff = le.getFeetBlockState().is(Blocks.SOUL_FIRE) || s.equals("soul") ? AlembicMobEffectRegistry.SOUL_FIRE.get() : AlembicMobEffectRegistry.FIRE.get();
        if (le.hasEffect(eff)) {
            if(le.getEffect(eff) == null) return;
            ((MobEffectInstanceAccessor)le.getEffect(eff)).setDuration(ticks);
        } else {
                le.addEffect(new MobEffectInstance(eff, ticks+2, 0, true, false));
        }
    }

    public static void removeFireEffect(LivingEntity le){
        if(le.level().isClientSide) return;
        if(le.hasEffect(AlembicMobEffectRegistry.FIRE.get())){
            le.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent(cap -> cap.setFireType("normal"));
            MobEffectInstance effect = le.getEffect(AlembicMobEffectRegistry.FIRE.get());
            if(effect == null) return;
            ((MobEffectInstanceAccessor)effect).setDuration(0);
        }
        if(le.hasEffect(AlembicMobEffectRegistry.SOUL_FIRE.get())){
            le.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent(cap -> cap.setFireType("normal"));
            MobEffectInstance effect = le.getEffect(AlembicMobEffectRegistry.SOUL_FIRE.get());
            if(effect == null) return;
            ((MobEffectInstanceAccessor)effect).setDuration(0);
        }
    }
}
