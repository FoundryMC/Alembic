package foundry.alembic;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import net.minecraft.resources.ResourceLocation;

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
}
