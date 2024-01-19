package foundry.alembic.override;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeManager;
import foundry.alembic.codecs.CodecUtil;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlembicOverride {
    public static final Codec<AlembicOverride> CODEC = Codec.unboundedMap(CodecUtil.ALEMBIC_RL_CODEC, Codec.FLOAT).comapFlatMap(
            map -> {
                float total = 0;
                Object2FloatMap<AlembicDamageType> retMap = new Object2FloatOpenHashMap<>();
                for (Map.Entry<ResourceLocation, Float> entry : map.entrySet()) {
                    if (!DamageTypeManager.containsKey(entry.getKey())) {
                        return DataResult.error(() -> "Damage type %s does not exist!".formatted(entry.getKey()));
                    }
                    retMap.put(DamageTypeManager.getDamageType(entry.getKey()), entry.getValue());
                    total += entry.getValue();
                }
                if (total != 1.0f) {
                    float finalTotal = total;
                    return DataResult.error(() -> "Total value is %s! All values must sum up to 1.0".formatted(finalTotal > 1.0f ? "too high" : "too low"));
                }
                return DataResult.success(new AlembicOverride(retMap));
            },
            alembicOverride -> {
                Map<ResourceLocation, Float> retMap = new HashMap<>();
                for (Object2FloatMap.Entry<AlembicDamageType> entry : alembicOverride.damages.object2FloatEntrySet()) {
                    retMap.put(entry.getKey().getId(), entry.getFloatValue());
                }
                return retMap;
            }
    );

    private final Object2FloatMap<AlembicDamageType> damages;
    private int priority;
    private ResourceLocation id;

    public AlembicOverride(Object2FloatMap<AlembicDamageType> damages) {
        this.damages = damages;
    }

    public Object2FloatMap<AlembicDamageType> getDamagePercents() {
        return Object2FloatMaps.unmodifiable(damages);
    }

    public ResourceLocation getId() {
        return id;
    }

    void setId(ResourceLocation id) {
        this.id = id;
    }

    void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        List<String> damageStrings = damages.object2FloatEntrySet().stream().map(entry -> "{"+entry.getKey().getId().toString() + " " + entry.getFloatValue()+"}").toList();
        return "AlembicOverride{" +
                "damages=" + damageStrings +
                ", priority=" + priority +
                ", id=" + id +
                '}';
    }
}
