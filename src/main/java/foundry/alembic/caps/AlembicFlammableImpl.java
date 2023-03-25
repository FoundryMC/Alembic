package foundry.alembic.caps;

import net.minecraft.nbt.CompoundTag;

public class AlembicFlammableImpl implements AlembicFlammable {
    private String fireType = "normal";

    @Override
    public String getFireType() {
        return fireType;
    }

    @Override
    public void setFireType(String type) {
        fireType = type;
    }

    @Override
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("fireType", fireType);
        return nbt;
    }

    @Override
    public void fromNBT(CompoundTag nbt) {
        fireType = nbt.getString("fireType");
    }
}
