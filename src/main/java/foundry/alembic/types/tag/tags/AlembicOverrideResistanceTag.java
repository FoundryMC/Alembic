package foundry.alembic.types.tag.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.damagesource.DamageSourceIdentifier;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.tag.AbstractTag;
import foundry.alembic.types.tag.AlembicTagType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.util.CodecUtil;
import foundry.alembic.util.ComposedData;
import foundry.alembic.util.ComposedDataTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

public class AlembicOverrideResistanceTag extends AbstractTag {
    public static final Codec<AlembicOverrideResistanceTag> CODEC = RecordCodecBuilder.create(instance ->
            createBase(instance).and(
                    instance.group(
                            ResourceLocation.CODEC.fieldOf("attribute").forGetter(alembicOverrideResistanceTag -> alembicOverrideResistanceTag.resistanceAttribute),
                            Codec.STRING.optionalFieldOf("type", "").forGetter(alembicOverrideResistanceTag -> alembicOverrideResistanceTag.type)
                    )
            ).apply(instance, AlembicOverrideResistanceTag::new)
    );

    private final ResourceLocation resistanceAttribute;
    private final String type;

    public AlembicOverrideResistanceTag(Set<TagCondition> conditions, ResourceLocation resistanceAttribute, String type) {
        super(conditions);
        this.resistanceAttribute = resistanceAttribute;
        this.type = type;
    }

    @Override
    public void handlePostParse(AlembicDamageType damageType) {
        damageType.setResistanceAttribute((RangedAttribute) ForgeRegistries.ATTRIBUTES.getValue(resistanceAttribute));
    }

    @Override
    public @NotNull AlembicTagType<?> getType() {
        return AlembicTagType.OVERRIDE_RESISTANCE;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " Attribute: %s".formatted(resistanceAttribute.toString());
    }
}
