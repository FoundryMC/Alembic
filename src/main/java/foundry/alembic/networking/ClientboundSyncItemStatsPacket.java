package foundry.alembic.networking;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import foundry.alembic.client.ClientPacketHandler;
import foundry.alembic.stats.item.ItemStat;
import foundry.alembic.stats.item.ItemStatManager;
import foundry.alembic.stats.item.slots.EquipmentSlotType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public record ClientboundSyncItemStatsPacket(Map<Item, Multimap<EquipmentSlotType, ItemStat>> map) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(map,
                (friendlyByteBuf, item) -> friendlyByteBuf.writeInt(BuiltInRegistries.ITEM.getId(item)),
                (friendlyByteBuf, multimap) -> {
                    Collection<Map.Entry<EquipmentSlotType, ItemStat>> entries = multimap.entries();
                    buf.writeInt(entries.size());
                    for (Map.Entry<EquipmentSlotType, ItemStat> entry : entries) {
                        friendlyByteBuf.writeJsonWithCodec(EquipmentSlotType.CODEC, entry.getKey());
                        friendlyByteBuf.writeJsonWithCodec(ItemStat.CODEC, entry.getValue());
                    }
                });
    }

    public static ClientboundSyncItemStatsPacket decode(FriendlyByteBuf buf) {
        Map<Item, Multimap<EquipmentSlotType, ItemStat>> map = buf.readMap(
                (friendlyByteBuf) -> BuiltInRegistries.ITEM.byId(friendlyByteBuf.readInt()),
                (friendlyByteBuf) -> {
                    int size = friendlyByteBuf.readInt();
                    Multimap<EquipmentSlotType, ItemStat> multimap = HashMultimap.create(size, 2);
                    for (int i = 0; i < size; i++) {
                        EquipmentSlotType slotType = friendlyByteBuf.readJsonWithCodec(EquipmentSlotType.CODEC);
                        ItemStat stat = friendlyByteBuf.readJsonWithCodec(ItemStat.CODEC);
                        multimap.put(slotType, stat);
                    }
                    return multimap;
                });
        return new ClientboundSyncItemStatsPacket(map);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ClientPacketHandler.handleSyncItemStats(this);
    }
}
