package foundry.alembic.damagesource;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public abstract class AlembicDamageSourceIdentifier implements StringRepresentable {
    public static final Codec<AlembicDamageSourceIdentifier> CODEC = Codec.either(
            DefaultWrappedSources.CODEC.xmap(DefaultWrappedSources::getIdentifier, identifier -> DefaultWrappedSources.valueOf(identifier.getSerializedName())),
            Codec.STRING.xmap(AlembicDamageSourceIdentifier::create, identifier -> identifier.sourceStr)
    ).xmap(
            either -> either.left().isPresent() ? either.left().get() : either.right().get(),
            identifier -> identifier instanceof SetDamageSourceIdentifier setId ? Either.left(setId) : Either.right((WrappedDamageSourceIdentifier) identifier)
    );

    private static final Interner<AlembicDamageSourceIdentifier> INTERNED = Interners.newWeakInterner();

    private AlembicDamageSourceIdentifier() {}

    public abstract boolean matches(DamageSource damageSource);
    public boolean matches(AlembicDamageSourceIdentifier damageSource) {
        return this == damageSource;
    }

    public static SetDamageSourceIdentifier createSet(String setName, String... sourceIds) {
        return (SetDamageSourceIdentifier)INTERNED.intern(new SetDamageSourceIdentifier(setName, sourceIds));
    }

    public static WrappedDamageSourceIdentifier create(String sourceId) {
        return (WrappedDamageSourceIdentifier)INTERNED.intern(new WrappedDamageSourceIdentifier(sourceId));
    }

    public static final class SetDamageSourceIdentifier extends AlembicDamageSourceIdentifier {

        private final Set<String> sourceSet;
        private final String wrappedName;

        private SetDamageSourceIdentifier(String wrappedName, String... sources) {
            this.sourceSet = Set.of(sources);
            this.wrappedName = wrappedName;
        }

        @Override
        public boolean matches(DamageSource damageSource) {
            return sourceSet.contains(damageSource.msgId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceSet);
        }

        @Override
        public String getSerializedName() {
            return wrappedName;
        }
    }

    public static final class WrappedDamageSourceIdentifier extends AlembicDamageSourceIdentifier {

        private final String sourceStr;

        private WrappedDamageSourceIdentifier(String sourceStr) {
            this.sourceStr = sourceStr;
        }

        @Override
        public boolean matches(DamageSource damageSource) {
            return this.sourceStr.equals(damageSource.msgId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceStr);
        }

        @Override
        public String getSerializedName() {
            return sourceStr;
        }
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

        public static final Codec<DefaultWrappedSources> CODEC = new EnumCodec<>(values(), DefaultWrappedSources::valueOf);

        private final String safeName;
        private final SetDamageSourceIdentifier identifier;

        DefaultWrappedSources(String safeName, DamageSource... sources) {
            this.safeName = safeName;
            this.identifier = AlembicDamageSourceIdentifier.createSet(safeName, Arrays.stream(sources).map(source -> source.msgId).toArray(String[]::new));
        }

        public SetDamageSourceIdentifier getIdentifier() {
            return identifier;
        }

        @Nonnull
        @java.lang.Override
        public String getSerializedName() {
            return safeName;
        }

        public static boolean exists(String name) {
            try {
                valueOf(name);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
    }
}
