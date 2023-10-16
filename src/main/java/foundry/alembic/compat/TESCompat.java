package foundry.alembic.compat;

import foundry.alembic.Alembic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tslat.tes.api.TESAPI;
import net.tslat.tes.api.TESParticle;
import net.tslat.tes.core.particle.type.NumericParticle;
import net.tslat.tes.core.state.EntityState;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class TESCompat {
    public static final ResourceLocation TES_CLAIMANT = Alembic.location("alembic_claimant");
    public static void spawnParticle (Level level, int entityID, String damageType, float damageAmount, int color) {
        EntityState state = TESAPI.getTESDataForEntity(entityID);
        LivingEntity e = (LivingEntity) level.getEntity(entityID);
        if (state != null && e != null) {
            CompoundTag data = new CompoundTag();
            data.putFloat("damage", damageAmount);
            data.putInt("color", color);
            TESAPI.submitParticleClaim(TES_CLAIMANT, e, data);
        }
    }

    public static void registerClaimant() {
        TESAPI.registerParticleClaimant(TES_CLAIMANT, (entityState, healthDelta, data, particleAdder) -> {
            if(data == null) return healthDelta;
            if(!data.contains("damage") || !data.contains("color")) return healthDelta;
            if(data.getFloat("damage") == 0) return healthDelta;
            Vector3f pos = new Vector3f(entityState.getEntity().getOnPos().getX() + 0.5f, entityState.getEntity().getOnPos().getY() + 0.5f, entityState.getEntity().getOnPos().getZ() + 0.5f);
            pos.add(0,entityState.getEntity().getBbHeight(),0);
            particleAdder.accept(new NumericParticle(entityState, pos, TESParticle.Animation.POP_OFF, data.getFloat("damage")).withColour(data.getInt("color")));
            return healthDelta + data.getFloat("damage");
        });
    }
}
