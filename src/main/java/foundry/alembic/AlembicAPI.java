package foundry.alembic;

import com.mojang.serialization.Codec;
import foundry.alembic.event.AlembicDamageEvent;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeManager;
import foundry.alembic.types.tag.AlembicTag;
import foundry.alembic.types.tag.AlembicTagRegistry;
import foundry.alembic.types.tag.AlembicTagType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.types.tag.condition.TagConditionRegistry;
import foundry.alembic.types.tag.condition.TagConditionType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class AlembicAPI {

    public static final ResourceKey<DamageType> SOUL_FIRE = ResourceKey.create(Registries.DAMAGE_TYPE, Alembic.location("soul_fire"));
    public static DamageSource soulFire(Entity entity){
        return new DamageSource(entity.level().registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(SOUL_FIRE), entity);
    }
    public static final ResourceKey<DamageType> ALCHEMICAL = ResourceKey.create(Registries.DAMAGE_TYPE, Alembic.location("alchemical"));
    public static DamageSource alchemical(Entity entity){
        return new DamageSource(entity.level().registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(ALCHEMICAL), entity);
    }

    public static final ResourceKey<DamageType> EVOKER_FANGS = ResourceKey.create(Registries.DAMAGE_TYPE, Alembic.location("evoker_fangs"));
    public static DamageSource evokerFangs(Entity entity){
        return new DamageSource(entity.level().registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(EVOKER_FANGS), entity);
    }

    public static final ResourceKey<DamageType> GUARDIAN_BEAM = ResourceKey.create(Registries.DAMAGE_TYPE, Alembic.location("guardian_beam"));
    public static DamageSource guardianBeam(Entity entity){
        return new DamageSource(entity.level().registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(GUARDIAN_BEAM), entity);
    }

    public static final ResourceKey<DamageType> ALLERGY = ResourceKey.create(Registries.DAMAGE_TYPE, Alembic.location("allergy"));
    public static DamageSource allergy(Entity entity){
        return new DamageSource(entity.level().registryAccess().registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(ALLERGY), entity);
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
        return DamageTypeManager.getDamageType(id);
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
