package foundry.alembic.override;

import foundry.alembic.types.AlembicDamageType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class AlembicOverride {
    private Override override;
    private float percentage;
    private String id;
    private int priority;
    private ResourceLocation entityType;

    public AlembicOverride(String id, int priority, Override override, float percentage) {
        this.id = id;
        this.priority = priority;
        this.override = override;
        this.percentage = percentage;
    }

    public void setEntityType(ResourceLocation entityType) {
        this.entityType = entityType;
    }

    public ResourceLocation getEntityType() {
        return entityType;
    }

    public float getPercentage() {
        return percentage;
    }


    public Override getOverride() {
        return override;
    }


    public String getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public enum Override {
        ENTITY_TYPE(),
        DROWN("drown"),
        FALL("fall"),
        PRICKED("cactus", "sweetBerryBush"),
        FIRE("inFire", "onFire", "hotFloor"),
        LAVA("lava"),
        SUFFOCATION("inWall"),
        CRAM("cramming"),
        STARVE("starve"),
        IMPACT("flyIntoWall"),
        OUT_OF_WORLD("outOfWorld"),
        GENERIC("generic"),
        MAGIC("magic"),
        WITHER("wither"),
        CRUSHED("anvil", "fallingBlock"),
        DRAGON_BREATH("dragonBreath"),
        DRIED_OUT("dryout"),
        FREEZE("freeze"),
        PIERCED("fallingStalactite","stalagmite"),
        ATTACK("mob", "player");

        private final List<String> sources;

        Override(String... sources) {
            this.sources = List.of(sources);
        }

        public List<String> getSources() {
            return sources;
        }
    }
}
