package foundry.alembic.items.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.items.ItemModifier;
import foundry.alembic.items.ItemModifierType;
import foundry.alembic.items.ItemStat;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Map;
import java.util.function.Function;

public record ReplaceItemModifier(Attribute target, Reference2FloatMap<Attribute> map) implements ItemModifier {
    public static final Codec<ReplaceItemModifier> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Registry.ATTRIBUTE.byNameCodec().fieldOf("target").forGetter(ReplaceItemModifier::target),
                    Codec.unboundedMap(Registry.ATTRIBUTE.byNameCodec(), Codec.FLOAT).fieldOf("replacements").xmap(
                            map -> (Reference2FloatMap<Attribute>)new Reference2FloatOpenHashMap<>(map),
                            Function.identity()
                    ).forGetter(replaceItemModifier -> replaceItemModifier.map)
            ).apply(instance, ReplaceItemModifier::new)
    );

    @Override
    public void compute(ItemStat.AttributeContainer container) {
        if (container.contains(target)) {

        }
    }

    @Override
    public ItemModifierType getType() {
        return ItemModifierType.REPLACE;
    }
}
