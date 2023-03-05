package foundry.alembic.types.tags;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.player.Player;

import java.util.Set;
import java.util.stream.Collectors;

public class AlembicGlobalTagPropertyHolder {
    private static final Int2ObjectMap<Set<AlembicPerLevelTag>> LEVELUP_BONUS = new Int2ObjectOpenHashMap<>();

    public static boolean deservesBonus(Player player) {
        return LEVELUP_BONUS.int2ObjectEntrySet().stream().anyMatch(setEntry -> player.experienceLevel % setEntry.getIntKey() == 0);
    }

    public static Set<AlembicPerLevelTag> getLevelupBonuses(Player player) {
        return LEVELUP_BONUS.int2ObjectEntrySet().stream().filter(setEntry -> player.experienceLevel % setEntry.getIntKey() == 0).flatMap(setEntry -> setEntry.getValue().stream()).collect(Collectors.toSet());
    }
}
