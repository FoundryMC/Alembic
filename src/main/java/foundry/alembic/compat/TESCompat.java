package foundry.alembic.compat;

import com.mojang.math.Vector3f;
import foundry.alembic.Alembic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tslat.tes.api.TESAPI;
import net.tslat.tes.api.TESParticle;
import net.tslat.tes.core.particle.type.TextParticle;
import net.tslat.tes.core.state.EntityState;

public class TESCompat {
    public static ResourceLocation TES_CLAIMANT = Alembic.location("alembic_claimant");
    public static void spawnParticle(Level level, int entityID, String damageType, float damageAmount, int color){
        EntityState state = TESAPI.getTESDataForEntity(entityID);
        LivingEntity e = (LivingEntity) level.getEntity(entityID);
        if(state != null){
            CompoundTag data = new CompoundTag();
            data.putFloat("damage", damageAmount);
            data.putInt("color", color);
            TESAPI.submitParticleClaim(TES_CLAIMANT, e, data);
        }
    }

    public static void registerClaimant(){
        TESAPI.registerParticleClaimant(TES_CLAIMANT, (entityState, healthDelta, data, particleAdder) -> {
            particleAdder.accept(new TextParticle(entityState, new Vector3f(entityState.getEntity().getEyePosition()), TESParticle.Animation.POP_OFF, ""+data.getFloat("damage")).withColour(data.getInt("color")));
            return healthDelta - data.getFloat("damage");
        });
    }
}
