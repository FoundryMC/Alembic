package foundry.alembic.types.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class AlembicMobEffect extends MobEffect {
    protected AlembicMobEffect(AlembicPotionDataHolder data) {
        super(MobEffectCategory.BENEFICIAL, data.getColor());
    }
}
