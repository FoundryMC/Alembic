package foundry.alembic;

import com.mojang.serialization.Codec;
import foundry.alembic.event.AlembicDamageEvent;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import foundry.alembic.types.tag.AlembicTag;
import foundry.alembic.types.tag.AlembicTagRegistry;
import foundry.alembic.types.tag.AlembicTagType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.types.tag.condition.TagConditionRegistry;
import foundry.alembic.types.tag.condition.TagConditionType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlembicAPI {

    public static final DamageSource SOUL_FIRE = new DamageSource("soulFire").setIsFire();
    public static final DamageSource ALCHEMICAL = new DamageSource("ALCHEMICAL");
    public static final DamageSource EVOKER_FANGS = new DamageSource("evokerFangs");
    public static final DamageSource GUARDIAN_BEAM = new DamageSource("guardianBeam");
    public static final DamageSource ALLERGY = new DamageSource("allergy");

    public static DamageSource indirectAlchemical(Entity pSource, @Nullable Entity pIndirectEntity) {
        return (new IndirectEntityDamageSource("indirectAlchemical", pSource, pIndirectEntity)).bypassArmor().setMagic();
    }

    /**
     * Registration method for alembic tag conditions. Create a tag condition to handle any needed conditional logic,
     * rather than hardcoding conditions in a tag.
     * @param id Id for this condition type. Must be in your mod's namespace
     * @param codec Codec for de/serializing your condition
     * @return Resulting condition type for static storage and use in condition class
     * @param <T> Your implementation of a TagCondition
     */
    public static <T extends TagCondition> TagConditionType<T> registerTagConditionType(ResourceLocation id, Codec<T> codec) {
        TagConditionType<T> type = () -> codec;
        TagConditionRegistry.register(id, type);
        return type;
    }

    /**
     * Registration method for alembic tags, used in Alembic damage types. Create a new tag type whenever Alembic's
     * tags don't quite cut it for your use case
     * @param id Id for this tag type. Must be in your mod's namespace
     * @param codec Codec for de/serializing your tag
     * @return Resulting tag type for static storage and use in tag class
     * @param <T> Your implementation of an AlembicTag
     */
    public static <T extends AlembicTag> AlembicTagType<T> registerTagType(ResourceLocation id, Codec<T> codec) {
        AlembicTagType<T> type = () -> codec;
        AlembicTagRegistry.register(id, type);
        return type;
    }

    public static AlembicDamageType getDamageType(ResourceLocation id) {
        return DamageTypeRegistry.getDamageType(id);
    }

    public static float activatePreEvent(LivingEntity target, LivingEntity attacker, AlembicDamageType damageType, float damage, float resistance) {
        AlembicDamageEvent.Pre event = new AlembicDamageEvent.Pre(target, attacker, damageType, damage, resistance);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getDamage();
    }

    public static float activatePostEvent(LivingEntity target, LivingEntity attacker, AlembicDamageType damageType, float damage, float resistance) {
        AlembicDamageEvent.Post event = new AlembicDamageEvent.Post(target, attacker, damageType, damage, resistance);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getDamage();
    }
}
