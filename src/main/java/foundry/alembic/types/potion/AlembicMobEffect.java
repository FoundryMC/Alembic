package foundry.alembic.types.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.tslat.effectslib.api.ExtendedMobEffect;

public class AlembicMobEffect extends ExtendedMobEffect {
    protected AlembicMobEffect(AlembicPotionDataHolder data) {
        super(MobEffectCategory.BENEFICIAL, data.getColor());
    }

    public AlembicMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
