package foundry.alembic.types.tag;

import com.mojang.serialization.Codec;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.types.tag.condition.conditions.ReferenceCondition;
import foundry.alembic.util.ComposedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public interface AlembicTag {
    Codec<AlembicTag> DISPATCH_CODEC = AlembicTagRegistry.TAG_MAP_CODEC.dispatch("tag_id", alembicTag -> {
        if (alembicTag.getType() == null) {
            throw new IllegalStateException("TagType for " + alembicTag.toString() + " is null");
        }
        return alembicTag.getType();
    }, AlembicTagType::getCodec);

    /**
     * Run when something is damaged with the parent damage type
     * @param data A collection of data passed in from the world
     */
    default void onDamage(ComposedData data) {}

    /**
     * Run every tick on an entity on the logical server
     * @param entity Entity that is being ticked
     * @param level Level the entity is in
     * @deprecated Not yet implemented
     */
    @Deprecated
    default void tick(LivingEntity entity, ServerLevel level) {/* TODO: implement? */}

    default boolean testConditions(ComposedData data) {
        return getConditions().stream().allMatch(tagCondition -> tagCondition.test(data));
    }

    List<TagCondition> getConditions();

    @Nonnull
    AlembicTagType<?> getType();

    /**
     * For handling anything that should be added to a tag after the damage type has been registered
     * @param damageType Registered damage type. Safe to reference
     */
    default void handlePostParse(AlembicDamageType damageType) {
    }
}
