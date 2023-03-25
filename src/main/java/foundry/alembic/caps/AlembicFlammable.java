package foundry.alembic.caps;

import net.minecraft.nbt.CompoundTag;

public interface AlembicFlammable {
    String getFireType();
    void setFireType(String type);
    CompoundTag toNBT();
    void fromNBT(CompoundTag nbt);
}
