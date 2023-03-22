package foundry.alembic.types.tags;

import com.mojang.serialization.Codec;

public interface AlembicTagType<T extends AlembicTag> {
    AlembicTagType<AlembicNoParticleTag> NO_PARTICLE = () -> AlembicNoParticleTag.CODEC;
    AlembicTagType<AlembicParticleTag> PARTICLE = () -> AlembicParticleTag.CODEC;
    AlembicTagType<AlembicExtendFireTag> EXTEND_FIRE = () -> AlembicExtendFireTag.CODEC;
    AlembicTagType<AlembicPerLevelTag> LEVEL_UP = () -> AlembicPerLevelTag.CODEC;
    AlembicTagType<AlembicHungerTag> HUNGER = () -> AlembicHungerTag.CODEC;
    AlembicTagType<AlembicAttributeReplacementTag> ATTRIBUTE_REPLACEMENT = () -> AlembicAttributeReplacementTag.CODEC;

    Codec<T> getCodec();

}