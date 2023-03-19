package foundry.alembic.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record ItemStat(ResourceLocation id, List<ItemStatAttributeData> attributes, String equipmentSlot) {
    public static final Codec<ItemStat> CODEC = RecordCodecBuilder.create(itemStatInstance ->
            itemStatInstance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(ItemStat::id),
                    ItemStatAttributeData.CODEC.listOf().fieldOf("attributes").forGetter(ItemStat::attributes),
                    Codec.STRING.fieldOf("equipment_slot").forGetter(ItemStat::equipmentSlot)
            ).apply(itemStatInstance, ItemStat::new)
    );

}
