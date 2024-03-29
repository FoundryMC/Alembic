package foundry.alembic.types;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import foundry.alembic.codecs.CodecUtil;
import foundry.alembic.util.ConditionalJsonResourceReloadListener;
import foundry.alembic.codecs.FileReferenceRegistryOps;
import foundry.alembic.util.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class DamageTypeManager extends ConditionalJsonResourceReloadListener {
    private static final Map<ResourceLocation, AlembicDamageType> DAMAGE_TYPES = new HashMap<>();

    public static final Codec<AlembicDamageType> DAMAGE_TYPE_CODEC = CodecUtil.ALEMBIC_RL_CODEC.comapFlatMap(
            resourceLocation -> {
                AlembicDamageType type = DAMAGE_TYPES.get(resourceLocation);
                if (type == null) {
                    return DataResult.error(() -> "Damage type " + resourceLocation + " does not exist!");
                }
                return DataResult.success(type);
            },
            AlembicDamageType::getId
    );

    private final RegistryAccess registryAccess;

    public DamageTypeManager(ICondition.IContext conditionContext, RegistryAccess registryAccess) {
        super(conditionContext, Utils.GSON, "alembic/damage_types");
        this.registryAccess = registryAccess;
    }

    public static void registerDamageType(ResourceLocation id, AlembicDamageType damageType) {
        DAMAGE_TYPES.put(id, damageType);
    }

    public static Collection<AlembicDamageType> getDamageTypes() {
        return Collections.unmodifiableCollection(DAMAGE_TYPES.values());
    }

    @Nullable
    public static AlembicDamageType getDamageType(ResourceLocation id) {
        return DAMAGE_TYPES.get(id);
    }

    public static Set<AlembicDamageType> getDamageTypes(Attribute attribute) {
        return DAMAGE_TYPES.values().stream().filter(damageType -> damageType.getAttribute() == attribute).collect(Collectors.toSet());
    }

    @Nullable
    public static AlembicDamageType getDamageType(String id) {
        return DAMAGE_TYPES.get(id.contains(":") ? new ResourceLocation(id) : Alembic.location(id));
    }

    public static boolean containsKey(ResourceLocation id) {
        return DAMAGE_TYPES.containsKey(id);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager rm, ProfilerFiller profiler) {
        DAMAGE_TYPES.clear();
        AlembicGlobalTagPropertyHolder.clearAll();
        FileReferenceRegistryOps<JsonElement> ops = FileReferenceRegistryOps.create(JsonOps.INSTANCE, registryAccess, rm);
        for (Map.Entry<ResourceLocation, JsonElement> entry : elements.entrySet()) {
            ResourceLocation id = entry.getKey();
            if (id.getPath().startsWith("tags/") || id.getPath().startsWith("conditions/")) {
                continue;
            }
            DataResult<AlembicDamageType> result = AlembicDamageType.CODEC.parse(ops, entry.getValue());
            if (result.error().isPresent()) {
                Alembic.LOGGER.error("Could not read %s. %s".formatted(id, result.error().get().message()));
                continue;
            }
            AlembicDamageType type = result.getOrThrow(false, Alembic.LOGGER::error);
            type.handlePostParse(id);

            if (containsKey(id)) {
                if (type.getPriority() < getDamageType(id).getPriority()) {
                    Alembic.LOGGER.debug("Damage type %s already exists with a higher priority. Skipping.".formatted(id));
                }
            } else {
                registerDamageType(id, type);
            }
        }
    }
}
