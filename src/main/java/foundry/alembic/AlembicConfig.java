package foundry.alembic;

import net.minecraftforge.common.ForgeConfigSpec;

//TODO: make-a da system
public class AlembicConfig {
    public static ForgeConfigSpec.BooleanValue modifyTooltips;
    public static ForgeConfigSpec.BooleanValue ownerAttributeProjectiles;
    public static ForgeConfigSpec.BooleanValue enableDebugPrints;
    public static ForgeConfigSpec.BooleanValue enableCompatFudge;


    public static ForgeConfigSpec makeConfig(ForgeConfigSpec.Builder builder) {
        modifyTooltips = builder.comment("Whether or not to modify tooltips to be vanilla friendly. (Turn off if using Apotheosis)").define("modify_tooltips", true);
        ownerAttributeProjectiles = builder.comment("Whether or not to apply the owner's damage attributes to projectiles. This disables entity-specific damage overrides for the projectile entities.").define("owner_attribute_projectiles", false);
        enableDebugPrints = builder.comment("Whether or not to enable debug prints. (Turn off if not debugging)").define("enable_debug_prints", false);
        enableCompatFudge = builder.comment("Whether or not to enable compat fudge. (Turn off if not debugging)").define("enable_compat_fudge", false);
        return builder.build();
    }
}
