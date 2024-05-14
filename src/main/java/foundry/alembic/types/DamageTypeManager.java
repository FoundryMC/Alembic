package foundry.alembic.types;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import foundry.alembic.Alembic;
import foundry.alembic.codecs.CodecUtil;
import foundry.alembic.networking.ClientboundSyncDamageTypesPacket;
import foundry.alembic.util.ConditionalCodecReloadListener;
import foundry.alembic.codecs.FileReferenceRegistryOps;
import foundry.alembic.util.Utils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DamageTypeManager extends ConditionalCodecReloadListener<AlembicDamageType> {
    private static final Map<ResourceLocation, AlembicDamageType> DAMAGE_TYPES = new HashMap<>();
    private static Map<ResourceLocation, AlembicDamageType> clientTypes = new HashMap<>();

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
        super(AlembicDamageType.CODEC, conditionContext, Utils.GSON, "alembic/damage_types");
        this.registryAccess = registryAccess;
    }

    public static void syncPacket(@Nullable Map<ResourceLocation, AlembicDamageType> damageTypeMap) {
        clientTypes = damageTypeMap;
    }

    public static ClientboundSyncDamageTypesPacket createPacket() {
        return new ClientboundSyncDamageTypesPacket(DAMAGE_TYPES);
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

    @Nullable
    public static AlembicDamageType getDamageType(String id) {
        return DAMAGE_TYPES.get(id.contains(":") ? new ResourceLocation(id) : Alembic.location(id));
    }

    public static boolean containsKey(ResourceLocation id) {
        return DAMAGE_TYPES.containsKey(id);
    }

    @Override
    protected void preApply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        DAMAGE_TYPES.clear();
        AlembicGlobalTagPropertyHolder.clearAll();
    }

    @Override
    protected boolean shouldParse(ResourceLocation id, JsonElement jsonElement) {
        return !id.getPath().startsWith("tags/") && !id.getPath().startsWith("conditions/");
    }

    @Override
    public DynamicOps<JsonElement> makeOps(ResourceManager resourceManager) {
        return FileReferenceRegistryOps.create(JsonOps.INSTANCE, registryAccess, resourceManager);
    }

    @Override
    protected void onSuccessfulParse(AlembicDamageType value, ResourceLocation id) {
        value.handlePostParse(id);

        AlembicDamageType existing = getDamageType(id);
        if (existing != null) {
            if (value.getPriority() < existing.getPriority()) {
                logger.debug("Damage type {} already exists with a higher priority. Skipping.", id);
            }
        } else {
            registerDamageType(id, value);
        }
    }
}
