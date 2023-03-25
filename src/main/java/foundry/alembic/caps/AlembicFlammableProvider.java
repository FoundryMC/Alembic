package foundry.alembic.caps;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlembicFlammableProvider implements ICapabilitySerializable<CompoundTag> {
    private final AlembicFlammable CAP;
    public AlembicFlammableProvider() {
        CAP = new AlembicFlammableImpl();
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == AlembicFlammableHandler.CAPABILITY ? LazyOptional.of(() -> CAP).cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return CAP.toNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CAP.fromNBT(nbt);
    }
}
