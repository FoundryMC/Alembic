package foundry.alembic.caps;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlembicFlammableProvider implements ICapabilitySerializable<CompoundTag> {
    private final AlembicFlammable backing;
    public AlembicFlammableProvider() {
        backing = new AlembicFlammableImpl();
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return AlembicFlammableHandler.CAPABILITY.orEmpty(cap, LazyOptional.of(() -> backing));
    }

    @Override
    public CompoundTag serializeNBT() {
        return backing.toNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backing.fromNBT(nbt);
    }
}
