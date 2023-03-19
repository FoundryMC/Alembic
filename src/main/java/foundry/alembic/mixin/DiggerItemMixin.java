package foundry.alembic.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import foundry.alembic.items.ItemStatHolder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(DiggerItem.class)
public class DiggerItemMixin {

    @Shadow public Multimap<Attribute, AttributeModifier> defaultModifiers;

    @Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void alembic$onInit(float pAttackDamageModifier, float pAttackSpeedModifier, Tier pTier, TagKey pBlocks, Item.Properties pProperties, CallbackInfo ci, ImmutableMultimap.Builder builder) {
        Multimap<Attribute, AttributeModifier> defaultModifiers;
        HashMultimap<Attribute, AttributeModifier> hashMultimap = HashMultimap.create();
        this.defaultModifiers.forEach(hashMultimap::put);
        defaultModifiers = hashMultimap;
        ((DiggerItem)(Object)this).defaultModifiers = defaultModifiers;
    }
}
