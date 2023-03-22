package foundry.alembic;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Set;

//TODO: make-a da system
public class AlembicConfig {
    public static ForgeConfigSpec.ConfigValue<? extends List<String>> damageTypes;
    public static ForgeConfigSpec.ConfigValue<? extends List<String>> potionEffects;

    public static ForgeConfigSpec.ConfigValue<? extends List<String>> particles;

    public static ForgeConfigSpec.BooleanValue modifyTooltips;
    public static ForgeConfigSpec.BooleanValue ownerAttributeProjectiles;


    public static ForgeConfigSpec makeConfig(ForgeConfigSpec.Builder builder) {
        damageTypes = builder.comment("List of config-initialized damage types").define("damage_types", AlembicAPI.getDefaultDamageTypes());
        potionEffects = builder.comment("List of config-initialized potion effects").define("potion_effects", AlembicAPI.getDefaultPotionEffects());
        particles = builder.comment("List of config-initialized particles").define("particles", AlembicAPI.getDefaultParticles());
        modifyTooltips = builder.comment("Whether or not to modify tooltips to be vanilla friendly. (Turn off if using Apotheosis)").define("modify_tooltips", true);
        ownerAttributeProjectiles = builder.comment("Whether or not to apply the owner's damage attributes to projectiles. This disables entity-specific damage overrides for the projectile entities.").define("owner_attribute_projectiles", false);
        return builder.build();
    }

    public List<String> getList() {
        return AlembicConfig.damageTypes.get();
    }

    public static void addDamageType(String name) {
        AlembicConfig.damageTypes.get().add(name);
    }
}
