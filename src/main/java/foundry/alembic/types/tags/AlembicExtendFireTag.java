package foundry.alembic.types.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;

public class AlembicExtendFireTag implements AlembicTag {
    public static final Codec<AlembicExtendFireTag> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("multiplier").forGetter(alembicExtendFireTag -> alembicExtendFireTag.multiplier),
                    Codec.STRING.listOf().fieldOf("ignored_sources").forGetter(alembicExtendFireTag -> alembicExtendFireTag.ignoredSources)
            ).apply(instance, AlembicExtendFireTag::new)
    );

    private final float multiplier;
    private final List<String> ignoredSources;

    public AlembicExtendFireTag(float multiplier, List<String> ignoredSources){
        this.multiplier = multiplier;
        this.ignoredSources = ignoredSources;
    }
    @Override
    public void run(ComposedData data) {

    }

    @Override
    public void run(Level level, LivingEntity entity, float damage, DamageSource originalSource) {
        if(entity.isOnFire() && !ignoredSources.contains(originalSource.msgId)){
            entity.setSecondsOnFire((entity.getRemainingFireTicks()/20) + (int)Math.ceil((damage*multiplier)));
        }
    }

    @Override
    public AlembicTagType<?> getType() {
        return AlembicTagType.EXTEND_FIRE;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " Multiplier: %s, Ignored sources: %s".formatted(multiplier, Arrays.toString(ignoredSources.toArray()));
    }
}
