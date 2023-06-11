package foundry.alembic.types.tag.condition.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import foundry.alembic.Alembic;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class TagOrElementPredicate<E> {
    public static final TagOrElementPredicate<?> EMPTY = new TagOrElementPredicate<>(new ExtraCodecs.TagOrElementLocation(Alembic.location("empty"), false), holder -> true);
    public static <T, E> TagOrElementPredicate<E> alwaysTrue() {
        return (TagOrElementPredicate<E>)EMPTY;
    }

    public static <T, E> Codec<TagOrElementPredicate<E>> codec(ResourceKey<? extends Registry<T>> registryKey, Function<ResourceLocation, Optional<T>> registryResolver, BiPredicate<E, TagKey<T>> isTagPredicate) {
        return ExtraCodecs.TAG_OR_ELEMENT_ID.comapFlatMap(
                tagOrElementLocation -> {
                    if (!tagOrElementLocation.tag()) {
                        Optional<T> element = registryResolver.apply(tagOrElementLocation.id());
                        return element.map(t -> DataResult.success(new TagOrElementPredicate<>(tagOrElementLocation, (E e) -> e == t)))
                                .orElseGet(() -> DataResult.error(() -> "Element %s not found!".formatted(tagOrElementLocation.id())));
                    }
                    return DataResult.success(new TagOrElementPredicate<>(tagOrElementLocation, new Predicate<E>() {
                        private final TagKey<T> tagKey = TagKey.create(registryKey, tagOrElementLocation.id());

                        @Override
                        public boolean test(E e) {
                            return isTagPredicate.test(e, tagKey);
                        }
                    }));
                },
                predicate -> predicate.location
        );
    }

    private final ExtraCodecs.TagOrElementLocation location;
    private final Predicate<E> predicate;

    public TagOrElementPredicate(ExtraCodecs.TagOrElementLocation tagOrElementLocation, Predicate<E> predicate) {
        this.location = tagOrElementLocation;
        this.predicate = predicate;
    }

    public boolean matches(E element) {
        return predicate.test(element);
    }
}
