package foundry.alembic.networking;

import foundry.alembic.Alembic;
import net.minecraft.nbt.CompoundTag;
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

    public static void init(){
        INSTANCE.registerMessage(id++, ClientboundAlembicDamagePacket.class, ClientboundAlembicDamagePacket::encode, ClientboundAlembicDamagePacket::decode, ClientboundAlembicDamagePacket::handle);
        INSTANCE.registerMessage(id++, ClientboundAlembicFireTypePacket.class, ClientboundAlembicFireTypePacket::encode, ClientboundAlembicFireTypePacket::decode, ClientboundAlembicFireTypePacket::handle);
    }

    public static void sendFirePacket(Entity entity, String type) {
        CompoundTag tag = new CompoundTag();
        tag.putString("fireType", type);
        tag.putInt("entityID", entity.getId());
        AlembicPacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(entity.getX(), entity.getY(), entity.getZ(), 128, entity.level().dimension())), new ClientboundAlembicFireTypePacket(tag));
    }
}
