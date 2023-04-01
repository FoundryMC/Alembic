package foundry.alembic.util;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import foundry.alembic.Alembic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

public class CodecUtil {
    public static final Codec<Integer> STRINGIFIED_LITERAL_COLOR_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                try {
                    int color = Integer.decode(s);
                    return DataResult.success(color);
                } catch (NumberFormatException e) {
                    return DataResult.error(e.getMessage());
                }
            },
            "#%06X"::formatted
    );

    public static final Codec<Integer> COLOR_CODEC = Codec.either(
            STRINGIFIED_LITERAL_COLOR_CODEC,
            Codec.INT
    ).xmap(
            either -> either.left().isPresent() ? either.left().get() : either.right().get(),
            Either::left
    );

    public static final Codec<ResourceLocation> ALEMBIC_RL_CODEC = Codec.STRING.comapFlatMap(
            s -> ResourceLocation.read(s.contains(":") ? s : Alembic.MODID + ":" + s),
            ResourceLocation::toString
    );

    @Nonnull
    private static <T extends Enum<T>> Function<String, T> enumFromString(Class<T> clazz) {
        try {
            return s -> T.valueOf(clazz, s);
        } catch (IllegalArgumentException e) {
            return s -> null;
        }
    }

    public static final Codec<UUID> STRING_UUID = ExtraCodecs.stringResolverCodec(UUID::toString, UUID::fromString);

    public static final Codec<EquipmentSlot> EQUIPMENT_SLOT_CODEC = ExtraCodecs.stringResolverCodec(Enum::name, enumFromString(EquipmentSlot.class));

    public static final Codec<AttributeModifier.Operation> OPERATION_CODEC = ExtraCodecs.stringResolverCodec(Enum::name, enumFromString(AttributeModifier.Operation.class));

    public static final Codec<RangedAttribute> RANGED_ATTRIBUTE_REGISTRY_CODEC = ForgeRegistries.ATTRIBUTES.getCodec().comapFlatMap(
            attribute -> {
                try {
                    return DataResult.success((RangedAttribute) attribute);
                } catch (ClassCastException e) {
                    return DataResult.error("The attribute " + ForgeRegistries.ATTRIBUTES.getKey(attribute) + " is not a ranged attribute");
                }
            },
            Function.identity()
    );

    public static <T> SetCodec<T> setOf(Codec<T> elementCodec) {
        return new SetCodec<>(elementCodec);
    }

    public static class SetCodec<A> implements Codec<Set<A>> {
        private final Codec<A> elementCodec;

        public SetCodec(Codec<A> codec) {
            this.elementCodec = codec;
        }

        @Override
        public <T> DataResult<Pair<Set<A>, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(consumerConsumer -> {
                ImmutableSet.Builder<A> builder = new ImmutableSet.Builder<>();
                Stream.Builder<T> failed = Stream.builder();
                AtomicReference<DataResult<Unit>> ref = new AtomicReference<>(DataResult.success(Unit.INSTANCE, Lifecycle.stable()));

                consumerConsumer.accept(t -> {
                    DataResult<Pair<A, T>> result = elementCodec.decode(ops, t);
                    result.error().ifPresent(e -> failed.add(t));
                    ref.setPlain(ref.getPlain().apply2stable((unit, o) -> {
                        builder.add(o.getFirst());
                        return unit;
                    }, result));
                });

                Pair<Set<A>, T> pair = Pair.of(builder.build(), ops.createList(failed.build()));
                return ref.getPlain().map(unit -> pair).setPartial(pair);
            });
        }

        @Override
        public <T> DataResult<T> encode(Set<A> input, DynamicOps<T> ops, T prefix) {
            ListBuilder<T> builder = ops.listBuilder();

            for (A a : input) {
                builder.add(elementCodec.encodeStart(ops, a));
            }

            return builder.build(prefix);
        }
    }

    public static <T, L, R> T resolveEither(Either<L, R> either, Function<L, T> leftFunc, Function<R, T> rightFunc) {
        return either.left().isPresent() ? leftFunc.apply(either.left().get()) : rightFunc.apply(either.right().get());
    }

    public static <L, R> Either<L, R> wrapEither(L left, R right) {
        if (left != null && right != null) {
            throw new IllegalArgumentException("Cannot create either because both elements are present. Only one must exist");
        }
        return left == null ? Either.right(right) : Either.left(left);
    }

    public static <L, R> Optional<Either<L, R>> optionalEither(L left, R right) {
        if (left == null && right == null) {
            return Optional.empty();
        }
        return Optional.of(wrapEither(left, right));
    }
}
