package foundry.alembic.damagesource;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class DamageSourceIdentifier implements StringRepresentable, Comparable<DamageSourceIdentifier> {
    private static final Interner<DamageSourceIdentifier> INTERNED = Interners.newWeakInterner();

    public static final DamageSourceIdentifier EMPTY = create("alembic:empty");

    public static final Codec<DamageSourceIdentifier> CODEC = Codec.STRING.xmap(DamageSourceIdentifier::create, DamageSourceIdentifier::getSerializedName);
    public static final Codec<Either<DefaultWrappedSource, DamageSourceIdentifier>> EITHER_CODEC = Codec.either(DefaultWrappedSource.CODEC, CODEC);

    private final String damageSourceId;

    private DamageSourceIdentifier(String wrappedName) {
        this.damageSourceId = wrappedName;
    }

    public boolean matches(DamageSource damageSource) {
        return damageSourceId.equals(damageSource.msgId);
    }
    public boolean matches(DamageSourceIdentifier damageSource) {
        return this == damageSource;
    }

    public static DamageSourceIdentifier create(String damageSourceId) {
        return INTERNED.intern(new DamageSourceIdentifier(damageSourceId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(damageSourceId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DamageSourceIdentifier that = (DamageSourceIdentifier) o;
        return Objects.equals(damageSourceId, that.damageSourceId);
    }

    @Override
    public String getSerializedName() {
        return damageSourceId;
    }

    @Override
    public int compareTo(@NotNull DamageSourceIdentifier o) {
        return damageSourceId.compareTo(o.damageSourceId);
    }

    @Override
    public String toString() {
        return "AlembicDamageSourceIdentifier{" +
                "damageSourceId='" + damageSourceId + '\'' +
                '}';
    }

    public enum DefaultWrappedSource implements StringRepresentable {
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

        public static final Codec<DefaultWrappedSource> CODEC = StringRepresentable.fromEnum(DefaultWrappedSource::values);

        private final String safeName;
        private final Set<DamageSourceIdentifier> identifiers;

        DefaultWrappedSource(String safeName, DamageSource... sources) {
            this.safeName = safeName;
            this.identifiers = Arrays.stream(sources).map(source -> DamageSourceIdentifier.create(source.msgId)).collect(Collectors.toSet());
        }

        public Set<DamageSourceIdentifier> getIdentifiers() {
            return identifiers;
        }

        @Nonnull
        @java.lang.Override
        public String getSerializedName() {
            return safeName;
        }
    }
}
