package foundry.alembic.override;

import foundry.alembic.types.AlembicDamageType;

public class AlembicOverride {
    private AlembicDamageType damageType;
    private Override override;
    private String id;
    private int priority;

    private enum Override {
        ENTITY_DAMAGE,
        DROWN,
        FALL,
        VOID,
        PRICKED,
        FIRE,
        LAVA,
        SUFFOCATION,
        CRAM,
        STARVE,
        IMPACT,
        OUT_OF_WORLD,
        GENERIC,
        MAGIC,
        WITHER,
        CRUSHED,
        DRAGON_BREATH,
        DRIED_OUT,
        FREEZE,
        PIERCED
    }
}
