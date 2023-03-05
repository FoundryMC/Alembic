package foundry.alembic.types.tags;

import com.mojang.serialization.Codec;

public interface AlembicTagType<T> {
    AlembicTagType<AlembicNoParticleTag> NO_PARTICLE = () -> AlembicNoParticleTag.CODEC;
    AlembicTagType<AlembicParticleTag> PARTICLE = () -> AlembicParticleTag.CODEC;
    AlembicTagType<AlembicExtendFireTag> EXTEND_FIRE = () -> AlembicExtendFireTag.CODEC;

    Codec<T> getCodec();

}