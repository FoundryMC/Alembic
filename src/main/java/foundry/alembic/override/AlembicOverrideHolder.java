package foundry.alembic.override;

import foundry.alembic.Alembic;
import foundry.alembic.damagesource.AlembicDamageSourceIdentifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

import java.util.HashMap;
import java.util.Map;

public class AlembicOverrideHolder {
    private static final Map<AlembicDamageSourceIdentifier, AlembicOverride> OVERRIDES = new HashMap<>();

    public static boolean containsKey(AlembicDamageSourceIdentifier sourceIdentifier) {
        return OVERRIDES.containsKey(sourceIdentifier);
    }

    public static Map<AlembicDamageSourceIdentifier, AlembicOverride> getOverrides() {
        return OVERRIDES;
    }

    public static AlembicOverride get(AlembicDamageSourceIdentifier sourceIdentifier) {
        return OVERRIDES.get(sourceIdentifier);
    }

    public static void add(AlembicDamageSourceIdentifier sourceIdentifier, AlembicOverride override) {
        OVERRIDES.put(sourceIdentifier, override);
    }

    public static void remove(AlembicDamageSourceIdentifier sourceIdentifier) {
        OVERRIDES.remove(sourceIdentifier);
    }

    public static void smartAddOverride(AlembicDamageSourceIdentifier sourceIdentifier, AlembicOverride override, ResourceLocation id) {
        Alembic.LOGGER.info("Adding override for " + sourceIdentifier.getSerializedName() + " with override " + override.getId());
        if (containsKey(sourceIdentifier)){
            String overrideName = sourceIdentifier.getSerializedName() + "_" + override.getId();
            String baseName = sourceIdentifier.getSerializedName() + "_" + get(sourceIdentifier).getId();
            if (overrideName.equals(baseName)){
                if (get(sourceIdentifier).getPriority() < override.getPriority()) {
                    Alembic.LOGGER.info("Replacing override for " + sourceIdentifier.getSerializedName() + " with override " + override.getId() + " from location " + id.toString() + " because it has a higher priority");
                    remove(sourceIdentifier);
                    add(sourceIdentifier, override);
                }
            } else {
                add(sourceIdentifier, override);
            }
        } else {
            add(sourceIdentifier, override);
        }
    }

    public static void addOverride(AlembicDamageSourceIdentifier sourceIdentifier, AlembicOverride override) {
        add(sourceIdentifier, override);
    }

    public static void removeOverride(AlembicDamageSourceIdentifier sourceIdentifier) {
        remove(sourceIdentifier);
    }

    public static void clearOverrides() {
        OVERRIDES.clear();
    }

    public static AlembicOverride getOverridesForSource(DamageSource source) {
        return OVERRIDES.get(AlembicDamageSourceIdentifier.create(source.msgId));
    }
}
