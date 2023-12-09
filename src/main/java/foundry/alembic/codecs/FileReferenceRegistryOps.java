package foundry.alembic.codecs;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FileReferenceRegistryOps<T> extends RegistryOps<T> {

    private static Map<ResourceLocation, Object> topLevelParsedObjects = new HashMap<>();
    private final ResourceManager resourceManager;

    protected FileReferenceRegistryOps(DynamicOps<T> pDelegate, RegistryOps.RegistryInfoLookup pLookupProvider, ResourceManager resourceManager) {
        super(pDelegate, pLookupProvider);
        this.resourceManager = resourceManager;
    }

    private static RegistryOps.RegistryInfoLookup memoizeLookup(final RegistryOps.RegistryInfoLookup pLookupProvider) {
        return new RegistryOps.RegistryInfoLookup() {
            private final Map<ResourceKey<? extends Registry<?>>, Optional<? extends RegistryOps.RegistryInfo<?>>> lookups = new HashMap<>();

            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> p_256043_) {
                return (Optional<RegistryOps.RegistryInfo<T>>) this.lookups.computeIfAbsent(p_256043_, pLookupProvider::lookup);
            }
        };
    }

    public static <T> FileReferenceRegistryOps<T> create(DynamicOps<T> ops, HolderLookup.Provider registries, ResourceManager resourceManager) {
        return new FileReferenceRegistryOps<>(ops, memoizeLookup(new RegistryOps.RegistryInfoLookup() {
            public <E> Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> registryKey) {
                return registries.lookup(registryKey).map((lookup) -> new RegistryInfo<>(lookup, lookup, lookup.registryLifecycle()));
            }
        }), resourceManager);
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public void addParsed(ResourceLocation fullPath, Object parsed) {
        topLevelParsedObjects.put(fullPath, parsed);
    }

    public boolean hasParsed(ResourceLocation fullPath) {
        return topLevelParsedObjects.containsKey(fullPath);
    }

    public <T> T getParsed(ResourceLocation fullPath) {
        return (T)topLevelParsedObjects.get(fullPath);
    }
}
