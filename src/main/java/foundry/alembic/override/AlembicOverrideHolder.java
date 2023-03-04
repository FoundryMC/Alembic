package foundry.alembic.override;

import com.mojang.datafixers.util.Pair;
import foundry.alembic.Alembic;
import foundry.alembic.types.AlembicDamageType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlembicOverrideHolder {
    private static List<Pair<AlembicDamageType, AlembicOverride>> OVERRIDES = new ArrayList<>();

    public static List<Pair<AlembicDamageType, AlembicOverride>> getOverrides() {
        return OVERRIDES;
    }

    public static boolean containsKey(AlembicDamageType damageType){
        for(Pair<AlembicDamageType, AlembicOverride> pair : OVERRIDES){
            if(pair.getFirst() == damageType) return true;
        }
        return false;
    }

    public static Pair<AlembicDamageType, AlembicOverride> get(AlembicDamageType damageType){
        for(Pair<AlembicDamageType, AlembicOverride> pair : OVERRIDES){
            if(pair.getFirst() == damageType) return pair;
        }
        return null;
    }

    public static void add(AlembicDamageType damageType, AlembicOverride override){
        OVERRIDES.add(Pair.of(damageType, override));
    }

    public static void remove(AlembicDamageType damageType){
        if(containsKey(damageType)){
            OVERRIDES.remove(get(damageType));
        }
    }

    public static void smartAddOverride(AlembicDamageType id, AlembicOverride override){
        Alembic.LOGGER.info("Adding override for " + id.getId() + " with override " + override.getOverride().name());
        if (containsKey(id)){
            String overrideName = id.getId().toString() + "_" + override.getOverride().name();
            String baseName = id.getId().toString() + "_" + get(id).getSecond().getOverride().name();
            if (get(id).getSecond().getPriority() < override.getPriority() && overrideName.equals(baseName)){
                remove(id);
                add(id, override);
            } else if (!overrideName.equals(baseName)){
                add(id, override);
            }
        } else {
            add(id, override);
        }
    }

    public static void addOverride(AlembicDamageType id, AlembicOverride override){
        add(id, override);
    }

    public static AlembicOverride getOverride(AlembicDamageType id){
        return get(id).getSecond();
    }

    public static void removeOverride(AlembicDamageType id){
        remove(id);
    }

    public static void clearOverrides(){
        OVERRIDES.clear();
    }

    public static List<Pair<AlembicDamageType, AlembicOverride>> getOverridesForSource(String source){
        List<Pair<AlembicDamageType, AlembicOverride>> list = new ArrayList<>();
            for(Pair<AlembicDamageType, AlembicOverride> entry : OVERRIDES){
            if (entry.getSecond().getOverride().name().equals(source)){
                list.add(new Pair<>(entry.getFirst(), entry.getSecond()));
            }
            if (entry.getSecond().getOverride().getSources().contains(source)){
                list.add(new Pair<>(entry.getFirst(), entry.getSecond()));
            }
            if(entry.getSecond().getEntityType() != null){
                if(entry.getSecond().getEntityType().toString().equals(source)){
                    list.add(new Pair<>(entry.getFirst(), entry.getSecond()));
                }
            }
            if(entry.getSecond().getModdedSource() != null){
                if(entry.getSecond().getModdedSource().equals(source)){
                    list.add(new Pair<>(entry.getFirst(), entry.getSecond()));
                }
            }
        }
        return list;
    }

    public static Pair<AlembicDamageType, AlembicOverride> findPair(String id){
        for(Pair<AlembicDamageType, AlembicOverride> entry : OVERRIDES){
            if(entry.getSecond().getEntityType().toString().equals(id)){
                return Pair.of(entry.getFirst(), entry.getSecond());
            }
            if(entry.getSecond().getOverride().name().equals(id)){
                return Pair.of(entry.getFirst(), entry.getSecond());
            }
        }
        return null;
    }
}
