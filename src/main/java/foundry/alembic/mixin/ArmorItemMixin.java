package foundry.alembic.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

@Mixin(ArmorItem.class)
public class ArmorItemMixin {
    @Shadow public Multimap<Attribute, AttributeModifier> defaultModifiers;

    @Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void alembic$onInit(ArmorMaterial p_40386_, ArmorItem.Type p_266831_, Item.Properties p_40388_, CallbackInfo ci) {
        Multimap<Attribute, AttributeModifier> defaultModifiers = HashMultimap.create();
        defaultModifiers.putAll(this.defaultModifiers);
        ((ArmorItem)(Object)this).defaultModifiers = defaultModifiers;
    }
}
