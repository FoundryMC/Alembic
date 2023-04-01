package foundry.alembic.types.tag.condition.predicates;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.util.CodecUtil;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class EntityPredicate {
    public static final Codec<EntityPredicate> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.either(ForgeRegistries.ENTITY_TYPES.getCodec(), TagKey.codec(Registry.ENTITY_TYPE_REGISTRY)).optionalFieldOf("entity_type").forGetter(entityPredicate -> CodecUtil.optionalEither(entityPredicate.entityType, entityPredicate.entityTypeTag)),
                    ForgeRegistries.ITEMS.getCodec().optionalFieldOf("held_item").forGetter(entityPredicate -> Optional.ofNullable(entityPredicate.heldItem))
            ).apply(instance, EntityPredicate::new)
    );
    public static final EntityPredicate EMPTY = new EntityPredicate();

    @Nullable
    private EntityType<?> entityType;
    @Nullable
    private TagKey<EntityType<?>> entityTypeTag;
    @Nullable
    private final Item heldItem; // TODO: Expand to ItemPredicate. Possibly an EquipmentPredicate to test all known equipment slots?

    public EntityPredicate(@Nonnull Optional<Either<EntityType<?>, TagKey<EntityType<?>>>> eitherOptional, @Nonnull Optional<Item> item) {
        eitherOptional.ifPresent(either -> CodecUtil.resolveEither(either, entityType1 -> entityType = entityType1, entityTypeTagKey -> entityTypeTag = entityTypeTagKey));
        this.heldItem = item.orElse(null);
    }

    private EntityPredicate() {
        entityType = null;
        entityTypeTag = null;
        heldItem = null;
    }

    public boolean match(@Nonnull Entity entity) {
        if ((entityType != null && entity.getType() != entityType) || (entityTypeTag != null && !entity.getType().is(entityTypeTag))) {
            return false;
        }
        if (heldItem != null && entity instanceof LivingEntity livingEntity) {
            return livingEntity.getMainHandItem().is(heldItem);
        }
        return true;
    }
}
