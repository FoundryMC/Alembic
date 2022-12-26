package foundry.alembic;

import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeJSONListener;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
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
        if (e.getSource().getDirectEntity() instanceof LivingEntity attacker) {
            LivingEntity target = e.getEntity();
            float totalDamage = e.getAmount();
            for(AlembicDamageType damageType : DamageTypeRegistry.getDamageTypes()){
                if(attacker.getAttribute(damageType.getAttribute()).getValue() > 0){
                    float attrValue = target.getAttributes().hasAttribute(damageType.getResistanceAttribute()) ? (float) target.getAttribute(damageType.getResistanceAttribute()).getValue() : 0;
                    float resistMult = damageType.hasResistance()? Math.max(0, 1 - attrValue) : 1;
                    totalDamage += attacker.getAttribute(damageType.getAttribute()).getValue() * resistMult;
                    damageType.getTags().forEach(tag -> {
                        tag.run(attacker.level, target);
                    });
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attack(final LivingAttackEvent e) {
    }

    private static DamageSource src(LivingEntity entity) {
        return entity instanceof Player p ? DamageSource.playerAttack(p) : DamageSource.mobAttack(entity);
    }
}