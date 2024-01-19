package foundry.alembic.attribute;

import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import foundry.alembic.stats.item.ItemStatManager;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class AlembicAttribute extends RangedAttribute implements IFormattableAttribute {
    private static Map<AlembicAttribute, UUID> cache = new IdentityHashMap<>();

    public AlembicAttribute(String descriptionId, double defaultValue, double min, double max) {
        super(descriptionId, defaultValue, min, max);
    }

    public static void clearCache() {
        cache.clear();
    }

    public void setBaseValue(double value){
        this.defaultValue = value;
    }

    public void setMinValue(double value){
        this.minValue = value;
    }

    public void setMaxValue(double value){
        this.maxValue = value;
    }

    public void setDescriptionId(String id){
        this.descriptionId = id;
    }

    @Override
    public @Nullable UUID getBaseUUID() {
        if (descriptionId.contains("resistance") || descriptionId.contains("shielding") || descriptionId.contains("absorption")) {
            return null;
        }

        AtomicReference<UUID> uuid = new AtomicReference<>(null);
        if (cache.containsKey(this)) {
            uuid.set(cache.get(this));
        } else {
            ItemStatManager.getStats().values().forEach((itemStat) -> {
                itemStat.asMap().values().forEach((itemModifier) -> {
                    itemModifier.stream().findFirst().ifPresent(mod -> {
                        mod.attributeData().stream().filter(att -> att.getAttribute() != null && att.getAttribute().equals(this)).findFirst().ifPresent(att -> {
                            uuid.set(att.getUUID());
                        });
                    });
                });
            });
            cache.put(this, uuid.get());
        }
        return uuid.get();
    }
}
