package foundry.alembic.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.util.CodecUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Map;

public record ItemStat(Item item, List<ItemStatAttributeData> attributeData, EquipmentSlot equipmentSlot) {
    public static final Codec<ItemStat> CODEC = RecordCodecBuilder.create(itemStatInstance ->
            itemStatInstance.group(
                    Registry.ITEM.byNameCodec().fieldOf("id").forGetter(ItemStat::item),
                    ItemStatAttributeData.CODEC.listOf().fieldOf("attributes").forGetter(ItemStat::attributeData),
                    CodecUtil.EQUIPMENT_SLOT_CODEC.fieldOf("equipment_slot").forGetter(ItemStat::equipmentSlot)
            ).apply(itemStatInstance, ItemStat::new)
    );

    public Map<Attribute, AttributeModifier> createAttributes() {
        Map<Attribute, AttributeModifier> modifierMap = new Reference2ObjectOpenHashMap<>();
        for (ItemStatAttributeData data : attributeData) {
            modifierMap.put(data.getAttribute(), data.createModifier());
        }
        return modifierMap;
    }
}