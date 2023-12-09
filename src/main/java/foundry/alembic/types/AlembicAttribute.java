package foundry.alembic.types;

import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import foundry.alembic.stats.item.ItemStatManager;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class AlembicAttribute extends RangedAttribute implements IFormattableAttribute {

    public AlembicAttribute(String descriptionId, double defaultValue, double min, double max) {
        super(descriptionId, defaultValue, min, max);
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
        AtomicReference<UUID> uuid = new AtomicReference<>(null);
        ItemStatManager.getStats().values().forEach((itemStat) -> {
            itemStat.asMap().values().forEach((itemModifier) -> {
                itemModifier.stream().findFirst().ifPresent(mod -> {
                    mod.attributeData().stream().filter(att -> att.getAttribute() != null && att.getAttribute().equals(this)).findFirst().ifPresent(att -> {
                        uuid.set(att.getUUID());
                    });
                });
            });
        });
        return uuid.get();
    }
}
