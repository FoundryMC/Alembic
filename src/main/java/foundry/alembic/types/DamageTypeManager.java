package foundry.alembic.types;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import foundry.alembic.util.ConditionalJsonResourceReloadListener;
import foundry.alembic.codecs.FileReferenceOps;
import foundry.alembic.util.Utils;
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

    public static final Codec<AlembicDamageType> DAMAGE_TYPE_CODEC = ResourceLocation.CODEC.xmap(DAMAGE_TYPES::get, AlembicDamageType::getId);

    public DamageTypeManager(ICondition.IContext conditionContext) {
        super(conditionContext, Utils.GSON, "alembic/damage_types");
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
        for (Map.Entry<ResourceLocation, JsonElement> entry : elements.entrySet()) {
            ResourceLocation id = entry.getKey();
            if (id.getPath().startsWith("tags/") || id.getPath().startsWith("conditions/")) {
                continue;
            }
            DataResult<AlembicDamageType> result = AlembicDamageType.CODEC.parse(FileReferenceOps.create(JsonOps.INSTANCE, rm), entry.getValue());
            if (result.error().isPresent()) {
                Alembic.LOGGER.error("Could not read %s. %s".formatted(id, result.error().get().message()));
                continue;
            }
            AlembicDamageType type = result.getOrThrow(false, Alembic.LOGGER::error);
            type.handlePostParse(id);


            if (containsKey(id)) {
                if (type.getPriority() < getDamageType(type.getId()).getPriority()) {
                    Alembic.LOGGER.debug("Damage type %s already exists with a higher priority. Skipping.".formatted(type.getId()));
                }
            } else {
                registerDamageType(id, type);
            }
        }
    }
}
