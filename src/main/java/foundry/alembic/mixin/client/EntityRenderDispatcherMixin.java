package foundry.alembic.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import foundry.alembic.caps.AlembicFlammableHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @ModifyVariable(method = "renderFlame", at = @At(value = "STORE"), ordinal = 0)
    private TextureAtlasSprite alembic$renderFlame(TextureAtlasSprite textureatlassprite, @Local Entity entity) {
        return entity.getCapability(AlembicFlammableHandler.CAPABILITY, null)
                .map(alembicFlammable -> alembicFlammable.getTextureLocation(0))
                .map(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS))
                .orElse(textureatlassprite);
    }

    @ModifyVariable(method = "renderFlame", at = @At(value = "STORE"), ordinal = 1)
    private TextureAtlasSprite alembic$renderFlame1(TextureAtlasSprite textureatlassprite, @Local Entity entity) {
        return entity.getCapability(AlembicFlammableHandler.CAPABILITY, null)
                .map(alembicFlammable -> alembicFlammable.getTextureLocation(1))
                .map(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS))
                .orElse(textureatlassprite);
    }
}
