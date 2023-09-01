package foundry.alembic.override;

import foundry.alembic.Alembic;
import foundry.alembic.damagesource.DamageSourceIdentifier;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public class AlembicOverrideHolder {
    private static final Map<DamageSourceIdentifier, AlembicOverride> OVERRIDES = new Reference2ObjectOpenHashMap<>(); // TODO: may not be possible

    public static boolean containsKey(DamageSourceIdentifier sourceIdentifier) {
        return OVERRIDES.containsKey(sourceIdentifier);
    }

    public static boolean containsKey(DamageSource damageSource) {
        return containsKey(DamageSourceIdentifier.create(damageSource.msgId));
    }

    public static Map<DamageSourceIdentifier, AlembicOverride> getOverrides() {
        return Collections.unmodifiableMap(OVERRIDES);
    }

    public static AlembicOverride get(DamageSourceIdentifier sourceIdentifier) {
        return OVERRIDES.get(sourceIdentifier);
    }

    private static void put(DamageSourceIdentifier sourceIdentifier, AlembicOverride override) {
        OVERRIDES.put(sourceIdentifier, override);
    }

    public static void smartAddOverride(DamageSourceIdentifier sourceIdentifier, AlembicOverride override) {
        Alembic.printInDebug(() -> "Adding override for " + sourceIdentifier.getSerializedName() + " with override " + override.getId());
        if (containsKey(sourceIdentifier)) {
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

    @Nullable
    public static AlembicOverride getOverridesForSource(DamageSource source) {
        return OVERRIDES.get(DamageSourceIdentifier.create(source.msgId));
    }
}
