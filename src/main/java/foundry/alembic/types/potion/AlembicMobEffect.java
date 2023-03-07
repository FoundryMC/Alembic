package foundry.alembic.types.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AlembicMobEffect extends MobEffect {
    protected AlembicMobEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }
}
