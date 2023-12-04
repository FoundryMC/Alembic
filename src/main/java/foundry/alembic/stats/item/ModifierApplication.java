package foundry.alembic.stats.item;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ModifierApplication implements StringRepresentable {
    INSTANT("instant"), USED("used");

    public static final Codec<ModifierApplication> CODEC = StringRepresentable.fromEnum(ModifierApplication::values);

    private final String name;

    ModifierApplication(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
