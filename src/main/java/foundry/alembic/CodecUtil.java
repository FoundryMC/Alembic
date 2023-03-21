package foundry.alembic;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Set;
import java.util.TreeSet;

public class CodecUtil {
    public static final Codec<Integer> STRINGIFIED_LITERAL_COLOR_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                try {
                    int color = Integer.decode(s);
                    return DataResult.success(color);
                } catch (NumberFormatException e) {
                    return DataResult.error(e.getMessage());
                }
            },
            "#%06X"::formatted
    );

    public static final Codec<Integer> COLOR_CODEC = Codec.either(
            STRINGIFIED_LITERAL_COLOR_CODEC,
            Codec.INT
    ).xmap(
            either -> either.left().isPresent() ? either.left().get() : either.right().get(),
            Either::left
    );

    public static final Codec<ResourceLocation> ALEMBIC_RL_CODEC = Codec.STRING.comapFlatMap(
            s -> ResourceLocation.read(s.contains(":") ? s : Alembic.MODID + ":" + s),
            ResourceLocation::toString
    );

    public static final Codec<EquipmentSlot> EQUIPMENT_SLOT_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                try {
                    EquipmentSlot slot = EquipmentSlot.valueOf(s);
                    return DataResult.success(slot);
                } catch (IllegalArgumentException e) {
                    return DataResult.error("Invalid equipment slot: " + s);
                }
            },
            Enum::name
    );

    public static final Codec<AttributeModifier.Operation> OPERATION_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                try {
                    AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(s);
                    return DataResult.success(operation);
                } catch (IllegalArgumentException e) {
                    return DataResult.error("Operation: " + s + " is invalid");
                }
            },
            Enum::name
    );
}
