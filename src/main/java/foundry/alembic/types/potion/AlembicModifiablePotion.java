package foundry.alembic.types.potion;

import net.minecraft.world.item.alchemy.Potion;

public class AlembicModifiablePotion extends Potion {

    public AlembicModifiablePotion(AlembicPotionDataHolder dataHolder) {
        dataHolder.trackPotion(this);
    }
}
