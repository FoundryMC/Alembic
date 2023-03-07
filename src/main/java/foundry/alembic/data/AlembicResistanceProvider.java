package foundry.alembic.data;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.io.IOException;

public class AlembicResistanceProvider implements DataProvider {
    private final String modid;

    public AlembicResistanceProvider(String modid) {
        this.modid = modid;
    }

    @Override
    public void run(CachedOutput pOutput) throws IOException {

    }

    @Override
    public String getName() {
        return "Alembic Resistance Provider for " + modid;
    }
}
