package foundry.alembic.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

// TODO: Do something to prevent dependency cycles
public abstract class FileReferenceCodec<T> implements Codec<T> {

    private final String path;
    private final String extension;
    private final Codec<T> referenceCodec;

    // TODO: Swap over to a FileToIdConverter for 1.20
    public FileReferenceCodec(String path, String extension, Codec<T> referenceCodec) {
        this.path = path;
        this.extension = extension;
        this.referenceCodec = referenceCodec;
    }

    public static <T> FileReferenceCodec<T> json(String path, Codec<T> referenceCodec) {
        return new FileReferenceCodec<T>(path, ".json", referenceCodec) {
            @Override
            protected <E> E parseFile(Resource resource) {
                try (BufferedReader reader = resource.openAsReader()) {
                    JsonElement element = Utils.GSON.getAdapter(JsonObject.class).fromJson(reader);
                    return (E)element;
                } catch (IOException e) {

                }
                throw new IllegalStateException("Could not parse resource");
            }
        };
    }

    @Override
    public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
        if (ops instanceof FileReferenceOps<T1> fileReferenceOps) {
            // This is in the alembic namespace by default instead of minecraft
            DataResult<ResourceLocation> refIdResult = CodecUtil.ALEMBIC_RL_CODEC.parse(ops, input);
            if (refIdResult.error().isPresent()) {
                return referenceCodec.parse(ops, input).map(t -> Pair.of(t, input));
            }
            DataResult<T> dataResult = refIdResult.flatMap(refId -> resolveData(fileReferenceOps, refId));
            return dataResult.map(t -> Pair.of(t, input));
        }
        return referenceCodec.decode(ops, input);
    }

    protected final <T1> DataResult<T> resolveData(FileReferenceOps<T1> ops, ResourceLocation refId) {
        ResourceLocation fullPath = new ResourceLocation(refId.getNamespace(), path + refId.getPath() + extension);
        DataResult<T> parsedDataResult;
        if (ops.hasParsed(fullPath)) {
            parsedDataResult = ops.getParsed(fullPath);
        } else {
            Optional<Resource> resource = ops.getResourceManager().getResource(fullPath);
            T1 parsedFile = resource.<T1>map(this::parseFile).orElseThrow();
            parsedDataResult = referenceCodec.parse(ops, parsedFile);
            ops.addParsed(fullPath, parsedDataResult);
        }
        return parsedDataResult;
    }

    protected abstract <E> E parseFile(Resource resource);

    @Override
    public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
        return null;
    }
}
