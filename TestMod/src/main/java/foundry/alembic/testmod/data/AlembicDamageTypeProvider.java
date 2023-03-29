package foundry.alembic.testmod.data;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.io.IOException;

public abstract class AlembicDamageTypeProvider implements DataProvider {



    @Override
    public void run(CachedOutput pOutput) throws IOException {

    }

    @Override
    public String getName() {
        return "Alembic Damage Type Provider";
    }

    public interface Exporter {

    }
}
