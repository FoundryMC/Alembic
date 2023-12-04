package foundry.alembic.stats.shield;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.util.CodecUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ShieldBlockStat(ItemStack item, List<TypeModifier> typeModifiers) {
    public static final Codec<ShieldBlockStat> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.ITEM_OR_STACK_CODEC.fieldOf("item").forGetter(ShieldBlockStat::item),
                    Codec.list(TypeModifier.CODEC).fieldOf("blocking_stats").forGetter(ShieldBlockStat::typeModifiers)
            ).apply(instance, ShieldBlockStat::new)
    );


    public record TypeModifier(ResourceLocation type, float modifier) {
        public static final Codec<TypeModifier> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("type").forGetter(TypeModifier::type),
                        Codec.FLOAT.fieldOf("modifier").forGetter(TypeModifier::modifier)
                ).apply(instance, TypeModifier::new)
        );
    }
}
