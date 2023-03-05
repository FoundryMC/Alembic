package foundry.alembic;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

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
            String::valueOf
    );

    public static final Codec<Integer> COLOR_CODEC = Codec.either(
            STRINGIFIED_LITERAL_COLOR_CODEC,
            Codec.INT
    ).xmap(
            either -> either.left().isPresent() ? either.left().get() : either.right().get(),
            Either::left
    );
}
