package foundry.alembic.types.tag.tags;

import foundry.alembic.types.AlembicDamageType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AlembicGlobalTagPropertyHolder {
    private static final Int2ObjectMap<Set<AlembicPerLevelTag>> LEVELUP_BONUS = new Int2ObjectOpenHashMap<>();
    private static final Map<AlembicDamageType, AlembicHungerTag> HUNGER_BONUS = new HashMap<>();

    public static void addLevelupBonus(AlembicPerLevelTag perLevelTag) {
        LEVELUP_BONUS.computeIfAbsent(perLevelTag.getLevelDifference(), i -> new HashSet<>()).add(perLevelTag);
    }

    public static void addHungerBonus(AlembicDamageType type, AlembicHungerTag hungerTag) {
        HUNGER_BONUS.put(type, hungerTag);
    }

    public static Set<AlembicPerLevelTag> getLevelupBonuses(Player player) {
        return LEVELUP_BONUS.int2ObjectEntrySet().stream().filter(setEntry -> player.experienceLevel % setEntry.getIntKey() == 0).flatMap(setEntry -> setEntry.getValue().stream()).collect(Collectors.toSet());
    }

    public static AlembicHungerTag getHungerBonus(AlembicDamageType type) {
        return HUNGER_BONUS.get(type);
    }

    public static Map<AlembicDamageType, AlembicHungerTag> getHungerBonuses() {
        return HUNGER_BONUS;
    }
}
