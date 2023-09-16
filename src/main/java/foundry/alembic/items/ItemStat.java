package foundry.alembic.items;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.items.modifiers.ItemModifier;
import foundry.alembic.items.slots.EquipmentSlotType;
import foundry.alembic.util.CodecUtil;
import foundry.alembic.util.TagOrElements;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

public record ItemStat(TagOrElements<Item> items, List<ItemModifier> attributeData, Set<EquipmentSlotType> equipmentSlots) {
    public static final Codec<ItemStat> CODEC = RecordCodecBuilder.create(itemStatInstance ->
            itemStatInstance.group(
                    TagOrElements.codec(Registry.ITEM).fieldOf("id").forGetter(ItemStat::items),
                    ItemModifier.DISPATCH_CODEC.listOf().fieldOf("modifiers").forGetter(ItemStat::attributeData),
                    Codec.either(EquipmentSlotType.CODEC, CodecUtil.setOf(EquipmentSlotType.CODEC)).fieldOf("equipment_slot")
                            .xmap(
                                    either -> either.map(Set::of, Function.identity()),
                                    slots -> slots.size() == 1 ? Either.left(slots.iterator().next()) : Either.right(slots)
                            ).forGetter(ItemStat::equipmentSlots)
            ).apply(itemStatInstance, ItemStat::new)
    );

    public List<ItemModifier> getItemModifiers() {
        return attributeData;
    }

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