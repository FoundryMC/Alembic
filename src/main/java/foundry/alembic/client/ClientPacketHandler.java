package foundry.alembic.client;

import foundry.alembic.compat.TESCompat;
import foundry.alembic.networking.ClientboundAlembicDamagePacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPacketHandler {
    public static void handleDamagePacket(ClientboundAlembicDamagePacket msg, Supplier<NetworkEvent.Context> ctx) {
        if(ModList.get().isLoaded("tslatentitystatus")){
            try{
                TESCompat.spawnParticle(Minecraft.getInstance().level, msg.entityID, msg.damageType, msg.damageAmount, msg.color);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
