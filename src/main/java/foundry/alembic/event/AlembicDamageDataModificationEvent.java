package foundry.alembic.event;

import foundry.alembic.util.ComposedData;
import net.minecraftforge.eventbus.api.Event;

public class AlembicDamageDataModificationEvent extends Event {
    private ComposedData data;

    public AlembicDamageDataModificationEvent(ComposedData data) {
        this.data = data;
    }

    public ComposedData getData() {
        return data;
    }
}
