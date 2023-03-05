package foundry.alembic.compat;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tslat.tes.api.TESAPI;
import net.tslat.tes.api.TESParticle;
import net.tslat.tes.core.particle.type.TextParticle;
import net.tslat.tes.core.state.EntityState;

public class TESCompat {
    public static void spawnParticle(Level level, int entityID, String damageType, float damageAmount, int color){
        EntityState state = TESAPI.getTESDataForEntity(entityID);
        Entity e = level.getEntity(entityID);
        if(state != null){
            Vec3 ePos = level.getEntity(entityID).position();
            Vector3f pos = new Vector3f((float)ePos.x, (float)ePos.y+1.5f, (float)ePos.z);
            TextParticle particle = new TextParticle(state, pos, TESParticle.Animation.POP_OFF, damageAmount+"");
            particle.setColour(color);
            TESAPI.addTESParticle(particle);
        }
    }
}
