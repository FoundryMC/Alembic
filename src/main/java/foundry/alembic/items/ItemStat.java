package foundry.alembic.items;

import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.items.slots.EquipmentSlotType;
import foundry.alembic.util.TagOrElements;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public record ItemStat(TagOrElements<Item> items, List<ItemModifier> attributeData, EquipmentSlotType equipmentSlot) {
    public static final Codec<ItemStat> CODEC = RecordCodecBuilder.create(itemStatInstance ->
            itemStatInstance.group(
                    TagOrElements.codec(Registry.ITEM).fieldOf("id").forGetter(ItemStat::items),
                    ItemModifier.DISPATCH_CODEC.listOf().fieldOf("modifiers").forGetter(ItemStat::attributeData),
                    EquipmentSlotType.CODEC.fieldOf("equipment_slot").forGetter(ItemStat::equipmentSlot)
            ).apply(itemStatInstance, ItemStat::new)
    );

    public void computeAttributes(Multimap<Attribute, AttributeModifier> originalModifiers,
                                  BiPredicate<Attribute, AttributeModifier> onPut,
                                  Consumer<Attribute> onRemove) {
        AttributeContainer container = new AttributeContainer() {
            @Override
            public boolean contains(Attribute attribute) {
                return originalModifiers.containsKey(attribute);
            }

            @Override
            public boolean put(Attribute attribute, AttributeModifier modifier) {
                return onPut.test(attribute, modifier);
            }

            @Override
            public Collection<AttributeModifier> get(Attribute attribute) {
                return originalModifiers.get(attribute);
            }

            @Override
            public void remove(Attribute attribute) {
                onRemove.accept(attribute);
            }
        };

        for (ItemModifier data : attributeData) {
            data.compute(container);
        }
    }

    public static abstract class AttributeContainer {

        public abstract boolean contains(Attribute attribute);

        public abstract boolean put(Attribute attribute, AttributeModifier modifier);

        public abstract Collection<AttributeModifier> get(Attribute attribute);

        public abstract void remove(Attribute attribute);
    }
}