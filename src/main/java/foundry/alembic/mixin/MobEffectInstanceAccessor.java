package foundry.alembic.mixin;


import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceAccessor {

    @Accessor
    int getDuration();

    @Accessor("duration")
    void setDuration(int duration);
}
