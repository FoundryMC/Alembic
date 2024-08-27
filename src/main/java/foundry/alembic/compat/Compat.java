package foundry.alembic.compat;

import net.minecraftforge.fml.ModList;

public final class Compat {
    public static boolean isTESLoaded() {
        return ModList.get().isLoaded("tslatentitystatus");
    }

    public static boolean isCuriosLoaded() {
        return ModList.get().isLoaded("curios");
    }

    public static void init() {
        if (isTESLoaded()) {
            TESCompat.registerClaimant();
        }
        if (isCuriosLoaded()) {
            CuriosCompat.addListeners();
        }
    }
}
