package foundry.alembic.networking;

import foundry.alembic.Alembic;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class AlembicPacketHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            Alembic.location("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int id = 0;

    public static void init() {
        INSTANCE.messageBuilder(ClientboundAlembicDamagePacket.class, id++)
                .encoder(ClientboundAlembicDamagePacket::encode)
                .decoder(ClientboundAlembicDamagePacket::decode)
                .consumerMainThread(ClientboundAlembicDamagePacket::handle)
                .add();
        INSTANCE.messageBuilder(ClientboundAlembicFireTypePacket.class, id++)
                .encoder(ClientboundAlembicFireTypePacket::encode)
                .decoder(ClientboundAlembicFireTypePacket::decode)
                .consumerMainThread(ClientboundAlembicFireTypePacket::handle)
                .add();
        INSTANCE.messageBuilder(ClientboundSyncItemStatsPacket.class, id++)
                .encoder(ClientboundSyncItemStatsPacket::encode)
                .decoder(ClientboundSyncItemStatsPacket::decode)
                .consumerMainThread(ClientboundSyncItemStatsPacket::handle)
                .add();
        INSTANCE.messageBuilder(ClientboundSyncShieldStatsPacket.class, id++)
                .encoder(ClientboundSyncShieldStatsPacket::encode)
                .decoder(ClientboundSyncShieldStatsPacket::decode)
                .consumerMainThread(ClientboundSyncShieldStatsPacket::handle)
                .add();
        INSTANCE.messageBuilder(ClientboundSyncDamageTypesPacket.class, id++)
                .encoder(ClientboundSyncDamageTypesPacket::encode)
                .decoder(ClientboundSyncDamageTypesPacket::decode)
                .consumerNetworkThread(ClientboundSyncDamageTypesPacket::handle)
                .add();
    }

    public static void sendFirePacket(Entity entity, String type) {
        if (!entity.level().isClientSide) {
            AlembicPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new ClientboundAlembicFireTypePacket(type, entity.getId()));
        }
    }

    public static void syncDataPackElements() {

    }
}
