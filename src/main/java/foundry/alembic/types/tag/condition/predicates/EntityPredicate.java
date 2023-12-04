package foundry.alembic.types.tag.condition.predicates;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class EntityPredicate {
    public static final EntityPredicate EMPTY = new EntityPredicate();

    public static final Codec<TagOrElementPredicate<EntityType<?>>> ENTITY_EITHER_PREDICATE = TagOrElementPredicate.codec(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), BuiltInRegistries.ENTITY_TYPE::getOptional, EntityType::is);
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

    private final TagOrElementPredicate<EntityType<?>> tagOrElementPredicate;
    private final ItemPredicate heldItem;
    // TODO: Possibly have an EquipmentPredicate to test all known equipment slots?

    public EntityPredicate(@Nonnull TagOrElementPredicate<EntityType<?>> tagOrElement, @Nonnull ItemPredicate item) {
        this.tagOrElementPredicate = tagOrElement;
        this.heldItem = item;
    }

    private EntityPredicate() {
        this.tagOrElementPredicate = TagOrElementPredicate.alwaysTrue();
        heldItem = ItemPredicate.EMPTY;
    }

    public boolean matches(@Nullable Entity entity) {
        if (this == EMPTY) {
            return true;
        }
        if (entity == null) {
            return false;
        }
        if (!tagOrElementPredicate.matches(entity.getType())) {
            return false;
        }
        if (heldItem != ItemPredicate.EMPTY) {
            if (entity instanceof LivingEntity livingEntity) {
                return heldItem.matches(livingEntity.getMainHandItem());
            }
            return false;
        }
        return true;
    }
}
