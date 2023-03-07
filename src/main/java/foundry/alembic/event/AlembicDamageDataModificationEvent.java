package foundry.alembic.event;

import foundry.alembic.types.tags.AlembicTag;
import net.minecraftforge.eventbus.api.Event;

public class AlembicDamageDataModificationEvent extends Event {
    private AlembicTag.ComposedData data;

    public AlembicDamageDataModificationEvent(AlembicTag.ComposedData data) {
        this.data = data;
    }

    public AlembicTag.ComposedData getData() {
        return data;
    }
}
