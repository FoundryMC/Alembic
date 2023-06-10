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
    public static final EntityPredicate EMPTY = new EntityPredicate();

    public static final Codec<TagOrElementPredicate<Entity>> ENTITY_EITHER_PREDICATE = TagOrElementPredicate.codec(Registry.ENTITY_TYPE_REGISTRY, Registry.ENTITY_TYPE::getOptional, (Entity entity, TagKey<EntityType<?>> tagKey) -> entity.getType().is(tagKey));
    public static final Codec<EntityPredicate> RECORD_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ENTITY_EITHER_PREDICATE.optionalFieldOf("entity_type", TagOrElementPredicate.alwaysTrue()).forGetter(entityPredicate -> entityPredicate.tagOrElementPredicate),
                    ItemPredicate.CODEC.optionalFieldOf("held_item", ItemPredicate.EMPTY).forGetter(entityPredicate -> entityPredicate.heldItem)
            ).apply(instance, EntityPredicate::new)
    );
    public static final Codec<EntityPredicate> CODEC = Codec.either(ENTITY_EITHER_PREDICATE, RECORD_CODEC).xmap(
            either -> either.map(tagOrElementPredicate -> new EntityPredicate(tagOrElementPredicate, ItemPredicate.EMPTY), Function.identity()),
            entityPredicate -> {
                if (entityPredicate.heldItem != null) {
                    return Either.right(entityPredicate);
                }
                return Either.left(entityPredicate.tagOrElementPredicate);
            }
    );

    private final TagOrElementPredicate<Entity> tagOrElementPredicate;
    private final ItemPredicate heldItem;
    // TODO: Possibly have an EquipmentPredicate to test all known equipment slots?

    public EntityPredicate(@Nonnull TagOrElementPredicate<Entity> tagOrElement, @Nonnull ItemPredicate item) {
        this.tagOrElementPredicate = tagOrElement;
        this.heldItem = item;
    }

    private EntityPredicate() {
        this.tagOrElementPredicate = TagOrElementPredicate.alwaysTrue();
        heldItem = ItemPredicate.EMPTY;
    }

    public boolean matches(@Nullable Entity entity) {
        if (!tagOrElementPredicate.matches(entity)) {
            return false;
        }
        if (entity instanceof LivingEntity livingEntity) {
            return heldItem.matches(livingEntity.getMainHandItem());
        }
        return true;
    }
}
