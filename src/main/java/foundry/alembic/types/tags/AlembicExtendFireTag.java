package foundry.alembic.types.tags;

import com.google.gson.JsonArray;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class AlembicExtendFireTag implements AlembicTag<Level, Entity, Float> {
    float multiplier;
    List<String> ignoredSources = new ArrayList<>();

    public AlembicExtendFireTag(AlembicTagDataHolder data){
        this.multiplier = (float)data.data.get(0);
        this.ignoredSources = (List<String>)data.data.get(1);
    }
    @Override
    public void run(Level level, Entity entity, Float aFloat) {

    }

    @Override
    public void run(Level level, LivingEntity entity, float damage, DamageSource originalSource) {
        if(entity.isOnFire() && !ignoredSources.contains(originalSource.msgId)){
            entity.setSecondsOnFire((entity.getRemainingFireTicks()/20) + (int)Math.ceil((damage*multiplier)));
            if(entity instanceof Player pl){
                pl.displayClientMessage(Component.literal("You are on fire for " + entity.getRemainingFireTicks()/20 + " seconds!"), true);
            }
        }
    }

    @Override
    public void handleData(JsonArray tagValues, List<AlembicTag<?,?,?>> tags, String tagId, ResourceLocation damageType) {
        for(int i = 0; i < tagValues.size(); i++){
            List<String> ignoredSources = new ArrayList<>();
            if(tagValues.get(i).getAsJsonObject().has("ignoredSources")){
                for(int j = 0; j < tagValues.get(i).getAsJsonObject().get("ignoredSources").getAsJsonArray().size(); j++){
                    ignoredSources.add(tagValues.get(i).getAsJsonObject().get("ignoredSources").getAsJsonArray().get(j).getAsString());
                }
            }
            AlembicTagDataHolder data = new AlembicTagDataHolder(tagValues.get(i).getAsJsonObject().get("multiplier").getAsFloat(), ignoredSources);
            tags.add(new AlembicExtendFireTag(data));
        }
    }
}
