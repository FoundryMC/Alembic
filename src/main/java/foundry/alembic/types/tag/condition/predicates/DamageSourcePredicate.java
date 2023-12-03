package foundry.alembic.types.tag.condition.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.util.TagOrElements;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class DamageSourcePredicate {
    public static final MapCodec<DamageSourcePredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntityPredicate.CODEC.optionalFieldOf("direct_entity", EntityPredicate.EMPTY).forGetter(damageSourcePredicate -> damageSourcePredicate.directEntityPredicate),
                    EntityPredicate.CODEC.optionalFieldOf("indirect_entity", EntityPredicate.EMPTY).forGetter(damageSourcePredicate -> damageSourcePredicate.indirectEntityPredicate),
                    TagOrElements.lazyCodec(Registries.DAMAGE_TYPE).optionalFieldOf("damage_source").forGetter(damageSourcePredicate -> Optional.ofNullable(damageSourcePredicate.damageTypeLazy))
            ).apply(instance, DamageSourcePredicate::new)
    );

    private final EntityPredicate directEntityPredicate;
    private final EntityPredicate indirectEntityPredicate;
    private TagOrElements.Lazy<DamageType> damageTypeLazy;

    public DamageSourcePredicate(EntityPredicate directEntity, EntityPredicate indirectEntity, Optional<TagOrElements.Lazy<DamageType>> damageTypeLazyOptional) {
        this.directEntityPredicate = directEntity;
        this.indirectEntityPredicate = indirectEntity;
        this.damageTypeLazy = damageTypeLazyOptional.orElse(null);
    }

    public boolean matches(Level level, DamageSource damageSource) {
        if (!directEntityPredicate.matches(damageSource.getDirectEntity())) {
            return false;
        }
        if (!indirectEntityPredicate.matches(damageSource.getEntity())) {
            return false;
        }
        if (damageTypeLazy != null && !damageTypeLazy.getElements(level.registryAccess()).contains(damageSource.type())) {
            return false;
        }
        return true;
    }
}
