package foundry.alembic.caps;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class AlembicFlammableImpl implements AlembicFlammable {
    private String fireType = NORMAL_FIRE;
    private Int2ObjectFunction<ResourceLocation> textureLoc = NORMAL_FIRE_TEXTURES;

    @Override
    public String getFireType() {
        return fireType;
    }

    @Override
    public void setFireType(String type) {
        fireType = type;
        switch (type) {
            case "normal" -> setTextureFunction(NORMAL_FIRE_TEXTURES);
            case "soul" -> setTextureFunction(SOUL_FIRE_TEXTURES);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(int i) {
        return textureLoc.apply(i);
    }

    @Override
    public void setTextureFunction(Int2ObjectFunction<ResourceLocation> texture) {
        this.textureLoc = texture;
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
