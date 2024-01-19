package foundry.alembic.networking;

import foundry.alembic.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundAlembicDamagePacket(int entityID, String damageType, float damageAmount, int color) {

    public void encode(FriendlyByteBuf buf){
        buf.writeInt(entityID);
        buf.writeUtf(damageType);
        buf.writeFloat(damageAmount);
        buf.writeInt(color);
    }

    public static ClientboundAlembicDamagePacket decode(FriendlyByteBuf buf){
        return new ClientboundAlembicDamagePacket(buf.readInt(), buf.readUtf(), buf.readFloat(), buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ClientPacketHandler.handleDamagePacket(this, ctx);
    }
}
