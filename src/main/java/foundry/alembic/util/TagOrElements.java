package foundry.alembic.util;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class TagOrElements<T> {
    public static <T> Codec<TagOrElements.Lazy<T>> lazyCodec(ResourceKey<? extends Registry<T>> registryKey) {
        return ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(
                tagOrElementLocation -> new TagOrElements.Lazy<>(registryKey, tagOrElementLocation),
                tTagOrElements -> tTagOrElements.tagOrElement
        );
    }

    public static <T> Codec<TagOrElements.Immediate<T>> codec(Registry<T> registry) {
        return ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(
                tagOrElementLocation -> new TagOrElements.Immediate<>(registry, tagOrElementLocation),
                tTagOrElements -> tTagOrElements.tagOrElement
        );
    }

    protected final ExtraCodecs.TagOrElementLocation tagOrElement;
    protected Set<T> resolvedSet;

    private TagOrElements(ExtraCodecs.TagOrElementLocation tagOrElementLocation) {
        this.tagOrElement = tagOrElementLocation;
    }

    protected abstract ResourceKey<? extends Registry<T>> getRegistryKey();

    @Override
    public String toString() {
        return "TagOrElements[" + getRegistryKey().toString() + " / " + tagOrElement.toString() + "]";
    }

    public static class Immediate<T> extends TagOrElements<T> {
        private final Registry<T> registry;

        private Immediate(Registry<T> registry, ExtraCodecs.TagOrElementLocation tagOrElementLocation) {
            super(tagOrElementLocation);
            this.registry = registry;
        }

        public Set<T> getElements() {
            if (resolvedSet == null) {
                if (tagOrElement.tag()) {
                    resolvedSet = registry.getOrCreateTag(TagKey.create(getRegistryKey(), tagOrElement.id())).stream().map(Holder::get).collect(Collectors.toSet());
                } else {
                    T element = registry.get(tagOrElement.id());
                    if (element == null) {
                        throw new IllegalStateException("Element is null for %s".formatted(tagOrElement));
                    }
                    resolvedSet = Set.of();
                }
            }
            return resolvedSet;
        }

        @Override
        protected ResourceKey<? extends Registry<T>> getRegistryKey() {
            return registry.key();
        }
    }

    public static class Lazy<T> extends TagOrElements<T> {
        private final ResourceKey<? extends Registry<T>> registryKey;

        private Lazy(ResourceKey<? extends Registry<T>> registryKey, ExtraCodecs.TagOrElementLocation tagOrElementLocation) {
            super(tagOrElementLocation);
            this.registryKey = registryKey;
        }

        public Set<T> getElements(RegistryAccess access) {
            if (resolvedSet == null) {
                Registry<T> registry = access.registryOrThrow(registryKey);
                if (tagOrElement.tag()) {
                    resolvedSet = registry.getOrCreateTag(TagKey.create(registryKey, tagOrElement.id())).stream().map(Holder::get).collect(Collectors.toSet());
                } else {
                    T element = registry.get(tagOrElement.id());
                    if (element == null) {
                        throw new IllegalStateException("Element is null for %s".formatted(tagOrElement));
                    }
                    resolvedSet = Set.of(element);
                }
            }
            return resolvedSet;
        }

        @Override
        protected ResourceKey<? extends Registry<T>> getRegistryKey() {
            return registryKey;
        }
    }
}
