package foundry.alembic.mixin.client;

import foundry.alembic.caps.AlembicFlammable;
import foundry.alembic.caps.AlembicFlammableHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @ModifyVariable(method = "renderFire", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;location()Lnet/minecraft/resources/ResourceLocation;", shift = At.Shift.BEFORE))
    private static TextureAtlasSprite alembic$renderFire(TextureAtlasSprite textureatlassprite) {
        Player player = Minecraft.getInstance().player;
        if(player == null) return textureatlassprite;
        return player.getCapability(AlembicFlammableHandler.CAPABILITY, null)
                .map(alembicFlammable -> alembicFlammable.getTextureLocation(1))
                .map(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS))
                .orElse(textureatlassprite);
    }
}
