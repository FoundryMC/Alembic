package foundry.alembic.caps;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public interface AlembicFlammable {
    String NORMAL_FIRE = "normal";
    String SOUL_FIRE = "soul";
    Int2ObjectFunction<ResourceLocation> DEFAULT_FIRE =  key -> key == 0 ? new ResourceLocation("block/fire_0") : new ResourceLocation("block/fire_1");
    String getFireType();
    void setFireType(String type);
    ResourceLocation getTextureLocation(int i);
    void setTextureFunction(Int2ObjectFunction<ResourceLocation> texture);
    CompoundTag toNBT();
    void fromNBT(CompoundTag nbt);
}
