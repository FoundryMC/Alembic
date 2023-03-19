package foundry.alembic.mixin;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import foundry.alembic.client.TooltipHelper;
import foundry.alembic.items.ItemUUIDAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Unique
    UUID baseAttackSpeedUUID = ItemUUIDAccess.getbaseAttackSpeedUUID();
    @Unique
    UUID baseAttackDamageUUID = ItemUUIDAccess.getbaseAttackDamageUUID();
    @Unique
    AttributeModifier mod;
    @Unique
    Map.Entry<Attribute, AttributeModifier> entry;
    @Unique
    Player player;

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;getId()Ljava/util/UUID;", ordinal = 0, shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void alembic$getTooltipLines(Player pPlayer, TooltipFlag pIsAdvanced, CallbackInfoReturnable<List<Component>> cir, List list, MutableComponent mutablecomponent, int j, EquipmentSlot[] var6, int var7, int var8, EquipmentSlot equipmentslot, Multimap multimap, Iterator var11, Map.Entry<Attribute, AttributeModifier> entry, AttributeModifier attributemodifier, double d0, boolean flag){
//        if(!(((ItemStack)(Object)this).getItem() instanceof ArmorItem)){
//            if(attributemodifier.getId() != ItemUUIDAccess.getbaseAttackSpeedUUID() || attributemodifier.getId() != ItemUUIDAccess.getbaseAttackDamageUUID()){
//                Pair<Double, Boolean> tooltip = TooltipHelper.handleVanillaTooltips(attributemodifier, (ItemStack)(Object)this, pPlayer, entry, d0, flag);
//                d0 = tooltip.getFirst();
//                flag = tooltip.getSecond();
//            }
//        }
        this.mod = attributemodifier;
        this.entry = entry;
        this.player = pPlayer;
    }

    @ModifyVariable(method = "getTooltipLines", at = @At("STORE"), ordinal = 0)
    private double alembic$modifyVariable(double d0){
        if(!(((ItemStack)(Object)this).getItem() instanceof ArmorItem)){
            if(this.mod == null || this.entry == null) return d0;
            if(mod.getId() != ItemUUIDAccess.getbaseAttackSpeedUUID() || mod.getId() != ItemUUIDAccess.getbaseAttackDamageUUID()){
                Pair<Double, Boolean> tooltip = TooltipHelper.handleVanillaTooltips(mod, (ItemStack)(Object)this, player, entry, d0, false);
                d0 = tooltip.getFirst();
                return d0;
            }
        }
        return d0;
    }

    @ModifyVariable(method = "getTooltipLines", at = @At("STORE"), ordinal = 0)
    private boolean alembic$modifyVariable(boolean flag){
        if(!(((ItemStack)(Object)this).getItem() instanceof ArmorItem)){
            if(this.mod == null || this.entry == null) return flag;
            if(mod.getId() != ItemUUIDAccess.getbaseAttackSpeedUUID() || mod.getId() != ItemUUIDAccess.getbaseAttackDamageUUID()){
                Pair<Double, Boolean> tooltip = TooltipHelper.handleVanillaTooltips(mod, (ItemStack)(Object)this, player, entry, 0, flag);
                flag = tooltip.getSecond();
                return flag;
            }
        }
        return flag;
    }
}
