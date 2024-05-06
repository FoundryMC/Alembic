package foundry.alembic.client;

import foundry.alembic.Alembic;
import foundry.alembic.caps.AlembicFlammableHandler;
import foundry.alembic.compat.TESCompat;
import foundry.alembic.networking.ClientboundAlembicDamagePacket;
import foundry.alembic.networking.ClientboundAlembicFireTypePacket;
import foundry.alembic.networking.ClientboundSyncItemStatsPacket;
import foundry.alembic.networking.ClientboundSyncShieldStatsPacket;
import foundry.alembic.stats.item.ItemStatManager;
import foundry.alembic.stats.shield.ShieldStatManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPacketHandler {
    public static void handleDamagePacket(ClientboundAlembicDamagePacket msg, Supplier<NetworkEvent.Context> ctx) {
        if(ModList.get().isLoaded("tslatentitystatus")){
            try{
                TESCompat.spawnParticle(Minecraft.getInstance().level, msg.entityID(), msg.damageType(), msg.damageAmount(), msg.color());
            } catch (Exception e) { // TODO: Why is this necessary?
                Alembic.LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void handleFireTypePacket(ClientboundAlembicFireTypePacket msg) {
        Level level = Minecraft.getInstance().level;
        if(level == null) return;
        Entity entity = level.getEntity(msg.entityId());
        if(entity != null){
            entity.getCapability(AlembicFlammableHandler.CAPABILITY, null).ifPresent((handler) -> {
                handler.setFireType(msg.fireType());
            });
        }
    }

    public static void handleSyncItemStats(ClientboundSyncItemStatsPacket packet) {
        ItemStatManager.syncPacket(packet.map());
    }

    public static void handleSyncShieldStats(ClientboundSyncShieldStatsPacket packet) {
        ShieldStatManager.syncPacket(packet.stats());
    }
}
