package foundry.alembic.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import foundry.alembic.caps.AlembicFlammableHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @WrapOperation(method = "renderFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/Material;sprite()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private static TextureAtlasSprite alembic$renderFire(Material instance, Operation<TextureAtlasSprite> original) {
        Player player = Minecraft.getInstance().player;
        if(player == null) {
            return original.call(instance);
        }
        return player.getCapability(AlembicFlammableHandler.CAPABILITY, null)
                .map(alembicFlammable -> alembicFlammable.getTextureLocation(1))
                .map(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS))
                .orElse(original.call(instance));
    }

}
