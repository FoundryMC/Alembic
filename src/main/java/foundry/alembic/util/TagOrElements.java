package foundry.alembic.util;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TagOrElements<T> {
    public static <T> Codec<TagOrElements<T>> codec(Registry<T> registry) {
        return ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(
                tagOrElementLocation -> new TagOrElements<>(registry, tagOrElementLocation),
                tTagOrElements -> tTagOrElements.tagOrElement
        );
    }

    private final Registry<T> registry;
    private final ExtraCodecs.TagOrElementLocation tagOrElement;
    private final Supplier<Set<T>> lazyTagSupplier;

    public TagOrElements(Registry<T> registry, ExtraCodecs.TagOrElementLocation tagOrElementLocation) {
        this.registry = registry;
        this.tagOrElement = tagOrElementLocation;
        this.lazyTagSupplier = Suppliers.memoize(() ->
                registry.getTag(TagKey.create(registry.key(), tagOrElement.id()))
                .map(holders -> holders.stream()
                        .map(Holder::get)
                        .collect(Collectors.toSet()))
                .orElse(Set.of()));
    }

    public Set<T> getElements() {
        if (tagOrElement.tag()) {
            return lazyTagSupplier.get();
        }
        T element = registry.get(tagOrElement.id());
        return element != null ? Set.of(element) : Set.of();
    }

    @Override
    public String toString() {
        return "TagOrElements[" + registry.toString() + " / " + tagOrElement.toString() + "]";
    }
}
