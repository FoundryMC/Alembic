package foundry.alembic.types.tag.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import foundry.alembic.types.tag.AbstractTag;
import foundry.alembic.types.tag.AlembicTagType;
import foundry.alembic.types.tag.condition.TagCondition;
import foundry.alembic.util.ComposedData;
import foundry.alembic.util.ComposedDataTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlembicSoundEventTag extends AbstractTag {
    public static final Codec<AlembicSoundEventTag> CODEC = RecordCodecBuilder.create(instance ->
            createBase(instance).and(
                    instance.group(
                            BuiltInRegistries.SOUND_EVENT.holderByNameCodec().fieldOf("sound_event").forGetter(tag -> tag.soundEvent),
                            Codec.FLOAT.optionalFieldOf("volume", 1.0f).forGetter(tag -> tag.volume),
                            Codec.FLOAT.optionalFieldOf("pitch", 1.0f).forGetter(tag -> tag.pitch)
                    )
            ).apply(instance, AlembicSoundEventTag::new)
    );

    private final Holder<SoundEvent> soundEvent;
    private final float volume;
    private final float pitch;

    public AlembicSoundEventTag(List<TagCondition> conditions, Holder<SoundEvent> soundEvent, float volume, float pitch) {
        super(conditions);
        this.soundEvent = soundEvent;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void onDamage(ComposedData data) {
        ServerLevel level = data.get(ComposedDataTypes.SERVER_LEVEL);
        LivingEntity target = data.get(ComposedDataTypes.TARGET_ENTITY);

        // using the player sound source since I'm equating this to the sword sweep sound event -d
        level.playSound(null, target, soundEvent.get(), SoundSource.PLAYERS, volume, pitch);
    }

    @NotNull
    @Override
    public AlembicTagType<?> getType() {
        return AlembicTagType.SOUND_EVENT;
    }
}
