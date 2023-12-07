package foundry.alembic.networking;

import foundry.alembic.client.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundAlembicFireTypePacket(String fireType, int entityId) {

    public void encode(FriendlyByteBuf buf){
        buf.writeUtf(fireType);
        buf.writeInt(entityId);
    }

    public static ClientboundAlembicFireTypePacket decode(FriendlyByteBuf buf) {
        return new ClientboundAlembicFireTypePacket(buf.readUtf(), buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> _ctx) {
        ClientPacketHandler.handleFireTypePacket(this);
    }
}
