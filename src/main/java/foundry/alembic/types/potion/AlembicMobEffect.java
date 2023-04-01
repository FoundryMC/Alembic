package foundry.alembic.types.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.tslat.effectslib.api.ExtendedMobEffect;

public class AlembicMobEffect extends ExtendedMobEffect {
    public AlembicMobEffect(Attribute attribute, AlembicPotionDataHolder data) {
        super(MobEffectCategory.BENEFICIAL, data.getColor());
        addAttributeModifier(attribute, data.getUUID().toString(), data.getValue(), data.getOperation());
    }
}
