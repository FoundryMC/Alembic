package foundry.alembic.items.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.items.ItemModifier;
import foundry.alembic.items.ItemModifierType;
import foundry.alembic.items.ItemStat;
import foundry.alembic.items.ModifierApplication;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.function.Function;

public class ReplaceItemModifier extends ItemModifier {
    public static final Codec<ReplaceItemModifier> CODEC = RecordCodecBuilder.create(instance ->
            base(instance).and(
                    instance.group(
                            Registry.ATTRIBUTE.byNameCodec().fieldOf("target").forGetter(replaceItemModifier -> replaceItemModifier.target),
                            Codec.unboundedMap(Registry.ATTRIBUTE.byNameCodec(), Codec.FLOAT).fieldOf("replacements").xmap(
                                    map -> (Reference2FloatMap<Attribute>)new Reference2FloatOpenHashMap<>(map),
                                    Function.identity()
                            ).forGetter(replaceItemModifier -> replaceItemModifier.map)
                    )
            ).apply(instance, ReplaceItemModifier::new)
    );

    private final Attribute target;
    private final Reference2FloatMap<Attribute> map;

    public ReplaceItemModifier(ModifierApplication application, Attribute target, Reference2FloatMap<Attribute> map) {
        super(application);
        this.target = target;
        this.map = map;
    }

    @Override
    public void compute(ItemStat.AttributeContainer container) {
        if (container.contains(target)) {
            // TODO: impl
        }
    }

    @Override
    public ItemModifierType getType() {
        return ItemModifierType.REPLACE;
    }
}
