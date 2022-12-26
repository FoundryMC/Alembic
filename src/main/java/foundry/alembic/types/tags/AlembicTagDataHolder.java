package foundry.alembic.types.tags;

import java.util.List;

public class AlembicTagDataHolder {
    public List<Object> data;

    public AlembicTagDataHolder(Object... data){
        this.data = List.of(data);
    }
}
