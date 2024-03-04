package foundry.alembic.stats.shield;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.codecs.CodecUtil;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

public record ShieldBlockStat(ItemStack item, List<TypeModifier> typeModifiers) {
    public static final Codec<ShieldBlockStat> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.ITEM_OR_STACK_CODEC.fieldOf("item").forGetter(ShieldBlockStat::item), // TODO: wehhh??
                    TypeModifier.CODEC.listOf().comapFlatMap(
                            typeModifiers1 -> {
                                if (typeModifiers1.isEmpty()) {
                                    return DataResult.error(() -> "Shield stat must define \"blocking_stats\"");
                                }
                                return DataResult.success(typeModifiers1);
                            },
                            Function.identity()
                    ).fieldOf("blocking_stats").forGetter(ShieldBlockStat::typeModifiers)
            ).apply(instance, ShieldBlockStat::new)
    );


    public record TypeModifier(AlembicDamageType type, float modifier) {
        public static final Codec<TypeModifier> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        DamageTypeManager.DAMAGE_TYPE_CODEC.fieldOf("type").forGetter(TypeModifier::type),
                        Codec.FLOAT.fieldOf("modifier").forGetter(TypeModifier::modifier)
                ).apply(instance, TypeModifier::new)
        );
    }
}
