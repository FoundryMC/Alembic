package foundry.alembic.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ComposedData {
    private final Map<ComposedDataType<?>, Object> data = new HashMap<>();
    private ComposedData() {}
    public static ComposedData createEmpty() {
        return new ComposedData();
    }

    public <T> ComposedData add(ComposedDataType<T> type, T obj) {
        data.put(type, obj);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ComposedDataType<T> type) {
        return (T) data.get(type);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOptional(ComposedDataType<T> type) {
        return Optional.ofNullable((T)data.get(type));
    }
}