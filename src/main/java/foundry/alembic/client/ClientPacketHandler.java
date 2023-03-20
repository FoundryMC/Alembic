package foundry.alembic.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.alembic.compat.TESCompat;
import foundry.alembic.networking.ClientboundAlembicDamagePacket;
import foundry.alembic.networking.ClientboundAlembicFireTypePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

public class ClientPacketHandler {
    public static String fireType = "";
    public static void handleDamagePacket(ClientboundAlembicDamagePacket msg, Supplier<NetworkEvent.Context> ctx) {
        if(ModList.get().isLoaded("tslatentitystatus")){
            try{
                TESCompat.spawnParticle(Minecraft.getInstance().level, msg.entityID, msg.damageType, msg.damageAmount, msg.color);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void handleFireTypePacket(ClientboundAlembicFireTypePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            fireType = msg.fireType;
        });
        ctx.get().setPacketHandled(true);
    }

    public static void replaceFire(){
        if(ClientPacketHandler.fireType.equals("soul")){
            TextureAtlasSprite textureAtlasSprite = RenderHelper.SOUL_FIRE_1.sprite();
            RenderSystem.setShaderTexture(0, textureAtlasSprite.atlas().location());
        }
    }
}
