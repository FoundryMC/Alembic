package foundry.alembic.types.tag.condition.predicates;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.util.CodecUtil;
import foundry.alembic.util.ToBooleanFunction;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public class EntityPredicate {
    public static final Codec<EntityPredicate> RECORD_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ExtraCodecs.TAG_OR_ELEMENT_ID.optionalFieldOf("entity_type").forGetter(entityPredicate -> Optional.ofNullable(entityPredicate.tagOrElement)),
                    ForgeRegistries.ITEMS.getCodec().optionalFieldOf("held_item").forGetter(entityPredicate -> Optional.ofNullable(entityPredicate.heldItem))
            ).apply(instance, EntityPredicate::new)
    );
    public static final Codec<EntityPredicate> CODEC = Codec.either(ExtraCodecs.TAG_OR_ELEMENT_ID, RECORD_CODEC).xmap(
            either -> {
                if (either.left().isPresent()) {
                    return new EntityPredicate(either.left(), Optional.empty());
                } else {
                    return either.right().get();
                }
            },
            entityPredicate -> {
                if (entityPredicate.heldItem != null) {
                    return Either.right(entityPredicate);
                }
                return Either.left(entityPredicate.tagOrElement);
            }
    );

    public static final EntityPredicate EMPTY = new EntityPredicate();

    @Nullable
    private final ExtraCodecs.TagOrElementLocation tagOrElement;
    @Nullable
    private final Item heldItem; // TODO: Expand to ItemPredicate. Possibly an EquipmentPredicate to test all known equipment slots?
    private ToBooleanFunction<EntityType<?>> entityTestFunction = this::resolveTagOrElement;

    public EntityPredicate(@Nonnull Optional<ExtraCodecs.TagOrElementLocation> tagOrElement, @Nonnull Optional<Item> item) {
        this.tagOrElement = tagOrElement.orElse(null);
        this.heldItem = item.orElse(null);
    }

    private EntityPredicate() {
        this.tagOrElement = null;
        heldItem = null;
    }

    private boolean resolveTagOrElement(EntityType<?> entityType) {
        if (tagOrElement == null) {
            entityTestFunction = type -> true;
            return entityTestFunction.apply(entityType);
        }
        if (tagOrElement.tag()) {
            entityTestFunction = new ToBooleanFunction<>() {
                TagKey<EntityType<?>> tagKey = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, tagOrElement.id());

                @Override
                public boolean apply(EntityType<?> entityType) {
                    return entityType.is(tagKey);
                }
            };
        } else {
            entityTestFunction = new ToBooleanFunction<>() {
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(tagOrElement.id());

                @Override
                public boolean apply(EntityType<?> entityType) {
                    return entityType == type;
                }
            };
        }
        return entityTestFunction.apply(entityType);
    }

    public boolean match(@Nonnull Entity entity) {
        if (entityTestFunction.apply(entity.getType())) {
            return false;
        }
        if (heldItem != null && entity instanceof LivingEntity livingEntity) {
            return livingEntity.getMainHandItem().is(heldItem);
        }
        return true;
    }
}
