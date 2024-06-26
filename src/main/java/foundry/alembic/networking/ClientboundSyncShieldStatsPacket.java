package foundry.alembic.networking;

import foundry.alembic.client.ClientPacketHandler;
import foundry.alembic.stats.shield.ShieldBlockStat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record ClientboundSyncShieldStatsPacket(List<ShieldBlockStat> stats) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(stats, (friendlyByteBuf, shieldBlockStat) -> friendlyByteBuf.writeWithCodec(NbtOps.INSTANCE, ShieldBlockStat.CODEC, shieldBlockStat));
    }

    public static ClientboundSyncShieldStatsPacket decode(FriendlyByteBuf buf) {
        List<ShieldBlockStat> stats = buf.readCollection(ObjectArrayList::new, friendlyByteBuf -> friendlyByteBuf.readWithCodec(NbtOps.INSTANCE, ShieldBlockStat.CODEC));
        return new ClientboundSyncShieldStatsPacket(stats);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ClientPacketHandler.handleSyncShieldStats(this);
    }
}
