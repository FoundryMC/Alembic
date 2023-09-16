package foundry.alembic.potion;

import net.minecraft.util.StringRepresentable;

public enum PotionModifier implements StringRepresentable {
    LONG("long"),
    STRONG("strong");

    private final String safeName;

    PotionModifier(String safeName) {
        this.safeName = safeName;
    }

    @Override
    public String getSerializedName() {
        return safeName;
    }
}
