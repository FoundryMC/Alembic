package foundry.alembic.types.tag;

import com.mojang.serialization.Codec;
import foundry.alembic.Alembic;
import foundry.alembic.types.tag.tags.AlembicExtendFireTag;
import foundry.alembic.types.tag.tags.AlembicHungerTag;
import foundry.alembic.types.tag.tags.AlembicParticleTag;
import foundry.alembic.types.tag.tags.AlembicPerLevelTag;

public interface AlembicTagType<T extends AlembicTag> {
    AlembicTagType<AlembicParticleTag> PARTICLE = register("particle_tag", AlembicParticleTag.CODEC);
    AlembicTagType<AlembicExtendFireTag> EXTEND_FIRE = register("extend_fire_tag", AlembicExtendFireTag.CODEC);
    AlembicTagType<AlembicPerLevelTag> LEVEL_UP = register("per_level_tag", AlembicPerLevelTag.CODEC);
    AlembicTagType<AlembicHungerTag> HUNGER = register("hunger_tag", AlembicHungerTag.CODEC);

    Codec<T> getCodec();

    static void bootstrap() {}

    private static <T extends AlembicTag> AlembicTagType<T> register(String id, Codec<T> codec) {
        AlembicTagType<T> type = () -> codec;
        AlembicTagRegistry.register(Alembic.location(id), type);
        return type;
    }
}