package foundry.alembic.types.tags;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AlembicGlobalTagPropertyHolder {
    private static final Int2ObjectMap<Set<AlembicPerLevelTag>> LEVELUP_BONUS = new Int2ObjectOpenHashMap<>();

    public static void add(AlembicPerLevelTag perLevelTag) {
        if (!LEVELUP_BONUS.containsKey(perLevelTag.getLevelDifference())) {
            LEVELUP_BONUS.put(perLevelTag.getLevelDifference(), new HashSet<>());
        }
        LEVELUP_BONUS.get(perLevelTag.getLevelDifference()).add(perLevelTag);
    }

    public static Set<AlembicPerLevelTag> getLevelupBonuses(Player player) {
        return LEVELUP_BONUS.int2ObjectEntrySet().stream().filter(setEntry -> player.experienceLevel % setEntry.getIntKey() == 0).flatMap(setEntry -> setEntry.getValue().stream()).collect(Collectors.toSet());
    }
}
