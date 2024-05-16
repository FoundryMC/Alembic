package foundry.alembic.networking;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import foundry.alembic.client.ClientPacketHandler;
import foundry.alembic.types.AlembicDamageType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public record ClientboundSyncDamageTypesPacket(BiMap<ResourceLocation, AlembicDamageType> damageTypeMap) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(damageTypeMap, FriendlyByteBuf::writeResourceLocation, (friendlyByteBuf, alembicDamageType) -> {
            friendlyByteBuf.writeWithCodec(NbtOps.INSTANCE, AlembicDamageType.NETWORK_CODEC, alembicDamageType);
        });
    }

    public static ClientboundSyncDamageTypesPacket decode(FriendlyByteBuf buf) {
        Map<ResourceLocation, AlembicDamageType> map = buf.readMap(FriendlyByteBuf::readResourceLocation, friendlyByteBuf -> friendlyByteBuf.readWithCodec(NbtOps.INSTANCE, AlembicDamageType.NETWORK_CODEC));
        return new ClientboundSyncDamageTypesPacket(HashBiMap.create(map));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ClientPacketHandler.handleSyncDamageTypes(this);
        ctx.get().setPacketHandled(true);
    }
}