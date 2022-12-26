package foundry.alembic;

import java.util.ArrayList;
import java.util.List;

public class AlembicAPI {
    private static List<String> damageTypes = new ArrayList<>();

    public static void addDefaultDamageType(String damageType) {
        damageTypes.add(damageType);
    }

    public static List<String> getDefaultDamageTypes() {
        return damageTypes;
    }
}
