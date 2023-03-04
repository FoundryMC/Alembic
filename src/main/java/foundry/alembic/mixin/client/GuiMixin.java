package foundry.alembic.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import foundry.alembic.client.AlembicOverlayRegistry;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Gui.class)
public abstract class GuiMixin extends GuiComponent {
//    @Shadow
//    private int screenHeight;
//
//    @Shadow
//    private int screenWidth;
//
//    @Shadow
//    protected abstract void renderTextureOverlay(ResourceLocation texture, float opacity);
//
//
//
//    @Unique
//    private Player plr;
//
//    @Unique
//    private String currentAttribute;
//
//    @Inject(method = "renderHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
//    private void alembic$renderHealthBar(PoseStack pPoseStack, Player pPlayer, int pX, int pY, int pHeight, int p_168694_, float p_168695_, int p_168696_, int p_168697_, int p_168698_, boolean p_168699_, CallbackInfo ci, Gui.HeartType gui$hearttype, int i, int j, int k, int l, int m, int n, int o, int p, int q){
//        plr = pPlayer;
//        for(String type : foundry.alembic.AlembicConfig.list.get()){
//            if(pPlayer.getAttributes().hasAttribute(DamageTypeRegistry.getDamageType(type).getAbsorptionAttribute())){
//                if(pPlayer.getAttribute(DamageTypeRegistry.getDamageType(type).getAbsorptionAttribute()).getValue() > 0 && m*2 < Mth.ceil(pPlayer.getAttribute(DamageTypeRegistry.getDamageType(type).getAbsorptionAttribute()).getValue())){
//                    currentAttribute = type;
//                    RenderSystem.setShaderTexture(0, AlembicOverlayRegistry.OVERLAYS.get(type));
//                    blit(pPoseStack, p, q, m*2 + 1 == Mth.ceil(pPlayer.getAttribute(DamageTypeRegistry.getDamageType(type).getAbsorptionAttribute()).getValue()) ? 9 : 0, i, 9, 9);
//                    RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
//                }
//            }
//        }
//    }
//
//    @WrapWithCondition(method = "renderHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V", ordinal = 2))
//    private boolean alembic$redirectation(Gui gui, PoseStack ps, Gui.HeartType ht, int p, int q, int i, boolean yea, boolean bl3){
//        if(currentAttribute != null){
//            return plr.getAttribute(DamageTypeRegistry.getDamageType(currentAttribute).getAbsorptionAttribute()).getValue() <= 0;
//        }
//        return true;
//    }
}
