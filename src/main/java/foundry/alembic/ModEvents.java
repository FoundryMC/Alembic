package foundry.alembic;

import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Alembic.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void onAttributeModification(final EntityAttributeModificationEvent event){
        for(EntityType<? extends LivingEntity> type : event.getTypes()) {
            for(AlembicDamageType damageType : DamageTypeRegistry.getDamageTypes()){
                event.add(type, damageType.getAttribute(), damageType.getBase());
                if(damageType.hasShielding()) event.add(type, damageType.getShieldAttribute(), 0);
                if(damageType.hasResistance()) event.add(type, damageType.getResistanceAttribute(), 0);
                if(damageType.hasAbsorption()) event.add(type, damageType.getAbsorptionAttribute(), 0);
            }
        }
    }

}
