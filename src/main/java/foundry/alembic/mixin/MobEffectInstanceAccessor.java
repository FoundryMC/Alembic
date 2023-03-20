package foundry.alembic.mixin;


import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceAccessor {

    @Accessor
    public int getDuration();

    @Accessor("duration")
    public void setDuration(int duration);
}
