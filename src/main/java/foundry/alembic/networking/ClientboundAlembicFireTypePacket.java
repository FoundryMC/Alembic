package foundry.alembic.networking;

import foundry.alembic.client.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundAlembicFireTypePacket {
    public CompoundTag fireType;

    public ClientboundAlembicFireTypePacket(CompoundTag fireType) {
        this.fireType = fireType;
    }

    public static void encode(ClientboundAlembicFireTypePacket msg, FriendlyByteBuf buf){
        buf.writeNbt(msg.fireType);
    }

    public static ClientboundAlembicFireTypePacket decode(FriendlyByteBuf buf){
        return new ClientboundAlembicFireTypePacket(buf.readNbt());
    }

    public static void handle(ClientboundAlembicFireTypePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientPacketHandler.handleFireTypePacket(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}
