package foundry.alembic.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.alembic.client.ClientPacketHandler;
import foundry.alembic.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @ModifyVariable(method = "renderFire", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;location()Lnet/minecraft/resources/ResourceLocation;", shift = At.Shift.BEFORE))
    private static TextureAtlasSprite alembic$renderFire(TextureAtlasSprite textureatlassprite) {
        if(ClientPacketHandler.fireType.equals("soul")){
            return RenderHelper.SOUL_FIRE_1.sprite();
        }
        return textureatlassprite;
    }
}
