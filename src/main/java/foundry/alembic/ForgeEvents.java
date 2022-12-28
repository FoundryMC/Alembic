package foundry.alembic;

import com.mojang.datafixers.util.Pair;
import foundry.alembic.override.AlembicOverride;
import foundry.alembic.override.AlembicOverrideHolder;
import foundry.alembic.override.OverrideJSONListener;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeJSONListener;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static foundry.alembic.Alembic.MODID;

@Mod.EventBusSubscriber(modid=MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
    }

    @SubscribeEvent
    public static void onJsonListener(AddReloadListenerEvent event){
        DamageTypeJSONListener.register(event);
        OverrideJSONListener.register(event);
    }

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event){
        if(event.getEntity().level.isClientSide) return;
        if(event.getItemStack().getItem() == Items.STICK){
            for(AlembicDamageType damageType : DamageTypeRegistry.getDamageTypes()){
                event.getEntity().sendSystemMessage(damageType.getVisualString());
                event.getEntity().sendSystemMessage(Component.literal(event.getEntity().getAttribute(damageType.getAttribute()).getValue()+"").withStyle(s -> s.withColor(ChatFormatting.GOLD)));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingJoin(EntityJoinLevelEvent event){
        if(event.getEntity().level.isClientSide) return;
        if(event.getEntity() instanceof LivingEntity le){
            for(AlembicDamageType damageType : DamageTypeRegistry.getDamageTypes()){
                le.getAttribute(damageType.getAttribute()).setBaseValue(damageType.getBase());
                if(damageType.hasShielding()){
                    if(le.getAttributes().hasAttribute(damageType.getShieldAttribute())){
                        le.getAttribute(damageType.getShieldAttribute()).setBaseValue(0);
                    }
                }
                if(damageType.hasResistance()){
                    if(le.getAttributes().hasAttribute(damageType.getResistanceAttribute())){
                        le.getAttribute(damageType.getResistanceAttribute()).setBaseValue(0);
                    }
                }
                if(damageType.hasAbsorption()){
                    if(le.getAttributes().hasAttribute(damageType.getAbsorptionAttribute())){
                        le.getAttribute(damageType.getAbsorptionAttribute()).setBaseValue(0);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingSpawn(final LivingSpawnEvent event){
    }

    private static boolean noRecurse = false;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hurt(final LivingHurtEvent e){
        if (e.getEntity().level.isClientSide) return;
        if (noRecurse) return;
        noRecurse = true;
        if(e.getSource().getDirectEntity() == null){
            noRecurse = false;
            LivingEntity target = e.getEntity();
            float totalDamage = e.getAmount();
            List<Pair<AlembicDamageType, AlembicOverride>> pairs = AlembicOverrideHolder.getOverridesForSource(e.getSource().msgId);
            boolean override = !pairs.isEmpty();
            if(override){
                handleTypedDamage(target, totalDamage, pairs);
                //target.hurt(e.getSource(), totalDamage);
                noRecurse = false;
                e.setCanceled(true);
            }
        }
        if (e.getSource().getDirectEntity() instanceof LivingEntity attacker) {
            LivingEntity target = e.getEntity();
            float totalDamage = e.getAmount();
            List<Pair<AlembicDamageType, AlembicOverride>> pairs = AlembicOverrideHolder.getOverridesForSource(ForgeRegistries.ENTITY_TYPES.getKey(attacker.getType()).toString());
            boolean entityOverride = !pairs.isEmpty();
            if(entityOverride){
                handleTypedDamage(target, totalDamage, pairs);
            }
            for(AlembicDamageType damageType : DamageTypeRegistry.getDamageTypes()){
                if(attacker.getAttribute(damageType.getAttribute()).getValue() > 0){
                    float damage = (float) attacker.getAttribute(damageType.getAttribute()).getValue();
                    float attrValue = target.getAttributes().hasAttribute(damageType.getResistanceAttribute()) ? (float) target.getAttribute(damageType.getResistanceAttribute()).getValue() : 0;
                    damage = CombatRules.getDamageAfterAbsorb(damage, attrValue, (float) target.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue());
                    handleDamageInstance(target, damageType, damage);
                }
            }
            int time = target.invulnerableTime;
            target.invulnerableTime = 0;
            if (totalDamage > 0.001) {
                target.hurt(src(attacker), totalDamage);
            }
            target.invulnerableTime = time;
        }
        noRecurse = false;
        e.setCanceled(true);
    }

    private static void handleDamageInstance(LivingEntity target, AlembicDamageType damageType, float damage) {
        if(damage > 0) {
            float finalDamage = damage;
            DamageTypeRegistry.getDamageTypes().stream().filter(s -> s.equals(damageType)).findFirst().get().getTags().forEach(r -> {
                r.run(target.level, target, finalDamage);
            });
            int invtime = target.invulnerableTime;
            target.invulnerableTime = 0;
            float health = target.getHealth();
            target.getCombatTracker().recordDamage(DamageSource.GENERIC, health, damage);
            target.setHealth(health - damage);
            target.gameEvent(GameEvent.ENTITY_DAMAGE);
            target.invulnerableTime = invtime;
        }
    }

    private static void handleTypedDamage(LivingEntity target, float totalDamage, List<Pair<AlembicDamageType, AlembicOverride>> types) {
        for(Pair<AlembicDamageType, AlembicOverride> pair : types){
            AlembicDamageType damageType = pair.getFirst();
            AlembicOverride override = pair.getSecond();
            float damage = totalDamage * override.getPercentage();
            if(damage <= 0) {
                Alembic.LOGGER.warn("Damage overrides are too high! Damage is being reduced to 0 for {}!", damageType.getId().toString());
                //totalDamage += damage;
                continue;
            }
            float attrValue = target.getAttributes().hasAttribute(damageType.getResistanceAttribute()) ? (float) target.getAttribute(damageType.getShieldAttribute()).getValue() : 0;
            damage = CombatRules.getDamageAfterAbsorb(damage, attrValue, (float) target.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue());
            float absorptionValue = target.getAttributes().hasAttribute(damageType.getAbsorptionAttribute()) ? (float) target.getAttribute(damageType.getAbsorptionAttribute()).getValue() : 0;
            if(absorptionValue > 0){
                float absorption = Math.min(absorptionValue, damage);
                absorptionValue -= absorption;
                absorptionValue = Math.max(0, absorptionValue);
                target.sendSystemMessage(Component.literal("Absorbed " + absorption + " damage!").withStyle(s -> s.withColor(damageType.getColor())));
                damage -= absorption;
                if(damageType.hasAbsorption()){
                    target.getAttribute(damageType.getAbsorptionAttribute()).setBaseValue(absorptionValue);
                }
            }
            handleDamageInstance(target, damageType, damage);
            target.sendSystemMessage(Component.literal("Took " + damage + " damage!"));
        }
        //return totalDamage;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attack(final LivingAttackEvent e) {
    }

    private static DamageSource src(LivingEntity entity) {
        return entity instanceof Player p ? DamageSource.playerAttack(p) : DamageSource.mobAttack(entity);
    }
}