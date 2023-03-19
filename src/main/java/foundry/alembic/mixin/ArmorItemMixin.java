package foundry.alembic.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import foundry.alembic.items.ItemStatHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.UUID;

@Mixin(ArmorItem.class)
public class ArmorItemMixin {
    @Shadow public Multimap<Attribute, AttributeModifier> defaultModifiers;

    @Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void alembic$onInit(ArmorMaterial pMaterial, EquipmentSlot pSlot, Item.Properties pProperties, CallbackInfo ci, ImmutableMultimap.Builder builder, UUID uuid) {
        Multimap<Attribute, AttributeModifier> defaultModifiers;
        HashMultimap<Attribute, AttributeModifier> hashMultimap = HashMultimap.create();
        this.defaultModifiers.forEach(hashMultimap::put);
        defaultModifiers = hashMultimap;
        ((ArmorItem)(Object)this).defaultModifiers = defaultModifiers;
    }
}
