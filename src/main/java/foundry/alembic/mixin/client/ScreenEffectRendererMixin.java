package foundry.alembic.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.alembic.caps.AlembicFlammableHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @Redirect(method = "renderFire", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
    private static void alembic$renderFire(int pShaderTexture, ResourceLocation pTextureId) {
        Player player = Minecraft.getInstance().player;
        if(player == null) {
            RenderSystem.setShaderTexture(pShaderTexture, pTextureId);
        } else {
            player.getCapability(AlembicFlammableHandler.CAPABILITY, null)
                    .map(alembicFlammable -> alembicFlammable.getTextureLocation(1))
                    .map(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)).ifPresent(textureAtlasSprite -> {
                        RenderSystem.setShaderTexture(pShaderTexture, textureAtlasSprite.atlasLocation());
                    });
        }
    }

}
