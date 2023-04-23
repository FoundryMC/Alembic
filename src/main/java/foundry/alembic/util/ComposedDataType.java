package foundry.alembic.util;

public class ComposedDataType<T> {
    private final Class<T> clazz;
    ComposedDataType(Class<T> clazzForType) {
        this.clazz = clazzForType;
    }
}