package foundry.alembic.types.tags;

import com.mojang.datafixers.util.Pair;
import foundry.alembic.types.AlembicDamageType;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.HashMap;
import java.util.Map;

public class AlembicGlobalTagPropertyHolder {
    public static Map<RangedAttribute, AlembicPerLevelDataHolder> LEVELUP_ATTRIBUTES = new HashMap<>();
}
