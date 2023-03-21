package foundry.alembic.override;

import foundry.alembic.Alembic;
import foundry.alembic.damagesource.AlembicDamageSourceIdentifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AlembicOverrideHolder {
    private static final Map<AlembicDamageSourceIdentifier, AlembicOverride> OVERRIDES = new HashMap<>();

    public static boolean containsKey(AlembicDamageSourceIdentifier sourceIdentifier) {
        return OVERRIDES.containsKey(sourceIdentifier);
    }

    public static Map<AlembicDamageSourceIdentifier, AlembicOverride> getOverrides() {
        return Collections.unmodifiableMap(OVERRIDES);
    }

    public static AlembicOverride get(AlembicDamageSourceIdentifier sourceIdentifier) {
        return OVERRIDES.get(sourceIdentifier);
    }

    private static void put(AlembicDamageSourceIdentifier sourceIdentifier, AlembicOverride override) {
        OVERRIDES.put(sourceIdentifier, override);
    }

    public static void smartAddOverride(AlembicDamageSourceIdentifier sourceIdentifier, AlembicOverride override) {
        Alembic.LOGGER.info("Adding override for " + sourceIdentifier.getSerializedName() + " with override " + override.getId());
        if (containsKey(sourceIdentifier)){
            if (get(sourceIdentifier).getPriority() < override.getPriority()) {
                Alembic.LOGGER.info("Replacing override for " + sourceIdentifier.getSerializedName() + " with override " + override.getId() + " because it has a higher priority");
                put(sourceIdentifier, override);
            }
        } else {
            put(sourceIdentifier, override);
        }
    }

    static void clearOverrides() {
        OVERRIDES.clear();
    }

    public static AlembicOverride getOverridesForSource(DamageSource source) {
        return OVERRIDES.get(AlembicDamageSourceIdentifier.create(source.msgId));
    }
}
