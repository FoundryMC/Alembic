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

public final class AlembicDamageSourceIdentifier implements StringRepresentable, Comparable<AlembicDamageSourceIdentifier> {
    private static final Interner<AlembicDamageSourceIdentifier> INTERNED = Interners.newWeakInterner();

    public static final Codec<AlembicDamageSourceIdentifier> CODEC = Codec.STRING.xmap(AlembicDamageSourceIdentifier::create, AlembicDamageSourceIdentifier::getSerializedName);
    public static final Codec<Either<DefaultWrappedSources, AlembicDamageSourceIdentifier>> EITHER_CODEC = Codec.either(DefaultWrappedSources.CODEC, CODEC);

    private final String damageSourceId;

    private AlembicDamageSourceIdentifier(String wrappedName) {
        this.damageSourceId = wrappedName;
    }

    public boolean matches(DamageSource damageSource) {
        return damageSourceId.equals(damageSource.msgId);
    }
    public boolean matches(AlembicDamageSourceIdentifier damageSource) {
        return this == damageSource;
    }

    public static AlembicDamageSourceIdentifier create(String damageSourceId) {
        return INTERNED.intern(new AlembicDamageSourceIdentifier(damageSourceId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(damageSourceId);
    }

    @Override
    public String getSerializedName() {
        return damageSourceId;
    }

    @Override
    public int compareTo(@NotNull AlembicDamageSourceIdentifier o) {
        return damageSourceId.compareTo(o.damageSourceId);
    }

    public enum DefaultWrappedSources implements StringRepresentable {
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

        public static final Codec<DefaultWrappedSources> CODEC = StringRepresentable.fromEnum(DefaultWrappedSources::values);

        private final String safeName;
        private final Set<AlembicDamageSourceIdentifier> identifiers;

        DefaultWrappedSources(String safeName, DamageSource... sources) {
            this.safeName = safeName;
            this.identifiers = Arrays.stream(sources).map(source -> AlembicDamageSourceIdentifier.create(source.msgId)).collect(Collectors.toSet());
        }

        public Set<AlembicDamageSourceIdentifier> getIdentifiers() {
            return identifiers;
        }

        @Nonnull
        @java.lang.Override
        public String getSerializedName() {
            return safeName;
        }
    }
}
