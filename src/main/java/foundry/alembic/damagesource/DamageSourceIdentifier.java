package foundry.alembic.damagesource;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
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

    public boolean matches(DamageType damageSource) {
        return damageSourceId.equals(damageSource.msgId());
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
        DROWN("drown", DamageTypes.DROWN),
        FALL("fall", DamageTypes.FALL),
        PRICKED("pricked", DamageTypes.CACTUS, DamageTypes.SWEET_BERRY_BUSH),
        FIRE("fire", DamageTypes.IN_FIRE, DamageTypes.ON_FIRE, DamageTypes.HOT_FLOOR),
        LAVA("lava", DamageTypes.LAVA),
        SUFFOCATION("suffocation", DamageTypes.IN_WALL),
        CRAM("cram", DamageTypes.CRAMMING),
        STARVE("starve", DamageTypes.STARVE),
        IMPACT("impact", DamageTypes.FLY_INTO_WALL),
        OUT_OF_WORLD("void", DamageTypes.FELL_OUT_OF_WORLD),
        GENERIC("generic", DamageTypes.GENERIC),
        MAGIC("magic", DamageTypes.MAGIC),
        WITHER("wither", DamageTypes.WITHER),
        CRUSHED("crushed", DamageTypes.FALLING_ANVIL, DamageTypes.FALLING_BLOCK),
        DRAGON_BREATH("dragon_breath", DamageTypes.DRAGON_BREATH),
        DRIED_OUT("dry_out", DamageTypes.DRY_OUT),
        FREEZE("freeze", DamageTypes.FREEZE),
        PIERCED("pierced", DamageTypes.FALLING_STALACTITE, DamageTypes.STALAGMITE),
        ATTACK("attack", DamageTypes.MOB_ATTACK, DamageTypes.PLAYER_ATTACK),
        MODDED("modded");

        public static final Codec<DefaultWrappedSource> CODEC = StringRepresentable.fromEnum(DefaultWrappedSource::values);

        private final String safeName;
        private final Set<DamageSourceIdentifier> identifiers;

        @SafeVarargs
        DefaultWrappedSource(String safeName, ResourceKey<DamageType>... sources) {
            this.safeName = safeName;
            this.identifiers = Arrays.stream(sources).map(source -> DamageSourceIdentifier.create(source.location().toString())).collect(Collectors.toSet());
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
