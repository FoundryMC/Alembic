package foundry.alembic.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class TagOrElements<T> {
    public static <T> Codec<TagOrElements.Lazy<T>> lazyCodec(ResourceKey<? extends Registry<T>> registryKey) {
        return ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(
                tagOrElementLocation -> new TagOrElements.Lazy<>(registryKey, tagOrElementLocation),
                tTagOrElements -> tTagOrElements.tagOrElement
        );
    }

    public static <T> Codec<BuiltInLazy<T>> builtInLazyCodec(Registry<T> registry) {
        return ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(
                tagOrElementLocation -> new BuiltInLazy<>(registry, tagOrElementLocation),
                tTagOrElements -> tTagOrElements.tagOrElement
        );
    }

    public static <T> Codec<Immediate<T>> codec(ResourceKey<? extends Registry<T>> registryKey, ICondition.IContext tagContext) {
        return Codec.either(RegistryFixedCodec.create(registryKey), TagKey.hashedCodec(registryKey)).xmap(
                either -> either.map(
                        tHolder -> new Immediate<>(Set.of(tHolder), registryKey, new ExtraCodecs.TagOrElementLocation(tHolder.unwrapKey().get().location(), false)),
                        tTagKey -> new Immediate<>(tagContext.getTag(tTagKey).stream().collect(Collectors.toUnmodifiableSet()), registryKey, new ExtraCodecs.TagOrElementLocation(tTagKey.location(), true))
                ),
                tImmediate -> {
                    if (tImmediate.tagOrElement.tag()) {
                        return Either.right(TagKey.create(registryKey, tImmediate.tagOrElement.id()));
                    } else {
                        return Either.left(tImmediate.resolvedSet.stream().findFirst().get());
                    }
                }
        );
    }

    protected final ExtraCodecs.TagOrElementLocation tagOrElement;
    protected Set<Holder<T>> resolvedSet;

    private TagOrElements(ExtraCodecs.TagOrElementLocation tagOrElementLocation) {
        this.tagOrElement = tagOrElementLocation;
    }

    protected abstract ResourceKey<? extends Registry<T>> getRegistryKey();

    public ExtraCodecs.TagOrElementLocation getTagOrElementLocation() {
        return this.tagOrElement;
    }

    @Override
    public String toString() {
        return "TagOrElements[" + getRegistryKey().toString() + " / " + tagOrElement.toString() + "]";
    }

    public static class Immediate<T> extends TagOrElements<T> {
        private final ResourceKey<? extends Registry<T>> registryKey;

        private Immediate(Set<Holder<T>> resolved, ResourceKey<? extends Registry<T>> registryKey, ExtraCodecs.TagOrElementLocation tagOrElementLocation) {
            super(tagOrElementLocation);
            this.registryKey = registryKey;
            this.resolvedSet = resolved;
        }

        public Set<Holder<T>> getElements() {
            return resolvedSet;
        }

        @Override
        protected ResourceKey<? extends Registry<T>> getRegistryKey() {
            return registryKey;
        }
    }

    public static class BuiltInLazy<T> extends TagOrElements<T> {
        private final Registry<T> registry;

        private BuiltInLazy(Registry<T> registry, ExtraCodecs.TagOrElementLocation tagOrElementLocation) {
            super(tagOrElementLocation);
            this.registry = registry;
        }

        public Set<Holder<T>> getElements() {
            if (resolvedSet == null) {
                if (tagOrElement.tag()) {
                    resolvedSet = registry.getOrCreateTag(TagKey.create(getRegistryKey(), tagOrElement.id())).stream().collect(Collectors.toSet());
                } else {
                    Holder<T> element = registry.getHolderOrThrow(ResourceKey.create(registry.key(), tagOrElement.id()));
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
            return registry.key();
        }
    }

    public static class Lazy<T> extends TagOrElements<T> {
        private final ResourceKey<? extends Registry<T>> registryKey;

        private Lazy(ResourceKey<? extends Registry<T>> registryKey, ExtraCodecs.TagOrElementLocation tagOrElementLocation) {
            super(tagOrElementLocation);
            this.registryKey = registryKey;
        }

        public Set<Holder<T>> getElements(RegistryAccess access) {
            if (resolvedSet == null) {
                Registry<T> registry = access.registryOrThrow(registryKey);
                if (tagOrElement.tag()) {
                    resolvedSet = registry.getOrCreateTag(TagKey.create(registryKey, tagOrElement.id())).stream().collect(Collectors.toSet());
                } else {
                    Holder<T> element = registry.getHolderOrThrow(ResourceKey.create(registryKey, tagOrElement.id()));
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
