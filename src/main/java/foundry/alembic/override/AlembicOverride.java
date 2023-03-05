package foundry.alembic.override;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nonnull;
import java.util.List;

public class AlembicOverride {
//    public static final Codec<AlembicOverride> CODEC = RecordCodecBuilder.create(instance ->
//            instance.group(
//                    Override.CODEC.fieldOf("")
//            )
//    );

    private Override override;
    private float percentage;
    private String id;
    private int priority;
    private ResourceLocation entityType;
    private String moddedSource;

    public AlembicOverride(String id, int priority, Override override, float percentage) {
        this.id = id;
        this.priority = priority;
        this.override = override;
        this.percentage = percentage;
    }

    public String getModdedSource() {
        return moddedSource;
    }

    public void setModdedSource(String moddedSource) {
        this.moddedSource = moddedSource;
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

    public enum Override implements StringRepresentable {
        ENTITY_TYPE("ENTITY_TYPE"),
        DROWN("DROWN", DamageSource.DROWN),
        FALL("FALL", DamageSource.FALL),
        PRICKED("PRICKED", DamageSource.CACTUS, DamageSource.SWEET_BERRY_BUSH),
        FIRE("FIRE", DamageSource.IN_FIRE, DamageSource.ON_FIRE, DamageSource.HOT_FLOOR),
        LAVA("LAVA", DamageSource.LAVA),
        SUFFOCATION("SUFFOCATION", DamageSource.IN_WALL),
        CRAM("CRAM", DamageSource.CRAMMING),
        STARVE("STARVE", DamageSource.STARVE),
        IMPACT("IMPACT", DamageSource.FLY_INTO_WALL),
        OUT_OF_WORLD("OUT_OF_WORLD", DamageSource.OUT_OF_WORLD),
        GENERIC("GENERIC", DamageSource.GENERIC),
        MAGIC("MAGIC", DamageSource.MAGIC),
        WITHER("WITHER", DamageSource.WITHER),
        CRUSHED("CRUSHED", DamageSource.ANVIL, DamageSource.FALLING_BLOCK),
        DRAGON_BREATH("DRAGON_BREATH", DamageSource.DRAGON_BREATH),
        DRIED_OUT("DRY_OUT", DamageSource.DRY_OUT),
        FREEZE("FREEZE", DamageSource.FREEZE),
        PIERCED("PIERCED", DamageSource.FALLING_STALACTITE, DamageSource.STALAGMITE),
        ATTACK("ATTACK", new DamageSource("mob"), new DamageSource("player")),
        MODDED("MODDED");

        public static final Codec<Override> CODEC = new EnumCodec<>(values(), Override::valueOf);

        private final String safeName;
        private final List<DamageSource> sources;

        Override(String safeName, DamageSource... sources) {
            this.safeName = safeName;
            this.sources = List.of(sources);
        }

        public List<DamageSource> getSources() {
            return sources;
        }

        @Nonnull
        @java.lang.Override
        public String getSerializedName() {
            return safeName;
        }
    }
}
