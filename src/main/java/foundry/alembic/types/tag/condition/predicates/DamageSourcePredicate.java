package foundry.alembic.types.tag.condition.predicates;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.damagesource.DamageSourceIdentifier;
import foundry.alembic.util.CodecUtil;
import net.minecraft.world.damagesource.DamageSource;

import java.util.Optional;

public class DamageSourcePredicate {
    public static final MapCodec<DamageSourcePredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntityPredicate.CODEC.optionalFieldOf("direct_entity", EntityPredicate.EMPTY).forGetter(damageSourcePredicate -> damageSourcePredicate.directEntityPredicate),
                    EntityPredicate.CODEC.optionalFieldOf("indirect_entity", EntityPredicate.EMPTY).forGetter(damageSourcePredicate -> damageSourcePredicate.indirectEntityPredicate),
                    DamageSourceIdentifier.EITHER_CODEC.optionalFieldOf("damage_source").forGetter(damageSourcePredicate -> CodecUtil.optionalEither(damageSourcePredicate.wrappedSource, damageSourcePredicate.sourceId))
            ).apply(instance, DamageSourcePredicate::new)
    );

    private final EntityPredicate directEntityPredicate;
    private final EntityPredicate indirectEntityPredicate;
    private DamageSourceIdentifier sourceId;
    private DamageSourceIdentifier.DefaultWrappedSource wrappedSource;

    public DamageSourcePredicate(EntityPredicate directEntity, EntityPredicate indirectEntity, Optional<Either<DamageSourceIdentifier.DefaultWrappedSource, DamageSourceIdentifier>> eitherOptional) {
        this.directEntityPredicate = directEntity;
        this.indirectEntityPredicate = indirectEntity;
        eitherOptional.ifPresent(either -> CodecUtil.resolveEither(either, defaultWrappedSource -> wrappedSource = defaultWrappedSource, damageSourceIdentifier -> sourceId = damageSourceIdentifier));
    }

    public boolean matches(DamageSource damageSource) {
        if (!directEntityPredicate.matches(damageSource.getDirectEntity())) {
            return false;
        }
        if (!indirectEntityPredicate.matches(damageSource.getEntity())) {
            return false;
        }
        if (sourceId != null && !sourceId.matches(damageSource)) {
            return false;
        }
        if (wrappedSource != null && !wrappedSource.getIdentifiers().contains(DamageSourceIdentifier.create(damageSource.msgId))) {
            return false;
        }
        return true;
    }
}
