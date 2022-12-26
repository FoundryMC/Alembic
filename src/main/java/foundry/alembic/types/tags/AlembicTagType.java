package foundry.alembic.types.tags;

public class AlembicTagType {
    private final Class<?> clazz;
    private final Class<?>[] args;

    public AlembicTagType(Class<?> clazz, Class<?>... args) {
        this.clazz = clazz;
        this.args = args;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Class<?>[] getArgs() {
        return args;
    }
}
