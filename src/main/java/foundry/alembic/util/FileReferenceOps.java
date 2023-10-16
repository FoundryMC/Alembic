package foundry.alembic.util;

import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashMap;
import java.util.Map;

public class FileReferenceOps<T> extends DelegatingOps<T> {

    private static Map<ResourceLocation, Object> topLevelParsedObjects = new HashMap<>();
    private final ResourceManager resourceManager;

    protected FileReferenceOps(DynamicOps<T> pDelegate, ResourceManager resourceManager) {
        super(pDelegate);
        this.resourceManager = resourceManager;
    }

    public static <T> FileReferenceOps<T> create(DynamicOps<T> ops, ResourceManager resourceManager) {
        return new FileReferenceOps<>(ops, resourceManager);
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
