package foundry.alembic.networking;

import foundry.alembic.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundAlembicDamagePacket {
    public final int entityID;
    public final String damageType;
    public final float damageAmount;
    public final int color;

    public ClientboundAlembicDamagePacket(int entityID, String damageType, float damageAmount, int color) {
        this.entityID = entityID;
        this.damageType = damageType;
        this.damageAmount = damageAmount;
        this.color = color;
    }

    public static void encode(ClientboundAlembicDamagePacket msg, FriendlyByteBuf buf){
        buf.writeInt(msg.entityID);
        buf.writeUtf(msg.damageType);
        buf.writeFloat(msg.damageAmount);
        buf.writeInt(msg.color);
    }

    public static ClientboundAlembicDamagePacket decode(FriendlyByteBuf buf){
        return new ClientboundAlembicDamagePacket(buf.readInt(), buf.readUtf(), buf.readFloat(), buf.readInt());
    }

    public static void handle(ClientboundAlembicDamagePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientPacketHandler.handleDamagePacket(msg, ctx);
        });
        ctx.get().setPacketHandled(true);
    }
}
