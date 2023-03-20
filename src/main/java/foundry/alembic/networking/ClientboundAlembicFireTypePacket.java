package foundry.alembic.networking;

import foundry.alembic.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundAlembicFireTypePacket {
    public final String fireType;

    public ClientboundAlembicFireTypePacket(String fireType) {
        this.fireType = fireType;
    }

    public static void encode(ClientboundAlembicFireTypePacket msg, FriendlyByteBuf buf){
        buf.writeUtf(msg.fireType);
    }

    public static ClientboundAlembicFireTypePacket decode(FriendlyByteBuf buf){
        return new ClientboundAlembicFireTypePacket(buf.readUtf());
    }

    public static void handle(ClientboundAlembicFireTypePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ClientPacketHandler.handleFireTypePacket(msg, ctx);
    }
}
