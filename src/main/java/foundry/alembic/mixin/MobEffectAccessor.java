package foundry.alembic.mixin;

import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEffect.class)
public interface MobEffectAccessor {
    @Accessor("color")
    void setColor(int color);
}
