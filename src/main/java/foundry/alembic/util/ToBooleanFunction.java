package foundry.alembic.util;

public interface ToBooleanFunction<T> {
    boolean apply(T t);
}
