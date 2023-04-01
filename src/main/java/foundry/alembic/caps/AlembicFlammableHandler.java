package foundry.alembic.caps;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class AlembicFlammableHandler {
    public static final Capability<AlembicFlammable> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
}
