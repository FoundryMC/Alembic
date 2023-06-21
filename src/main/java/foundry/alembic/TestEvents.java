package foundry.alembic;

import foundry.alembic.event.AlembicDamageEvent;
import foundry.alembic.event.AlembicFoodChangeEvent;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeRegistry;
import foundry.alembic.types.tag.tags.AlembicGlobalTagPropertyHolder;
import foundry.alembic.types.tag.tags.AlembicHungerTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Alembic.MODID)
public class TestEvents {
    @SubscribeEvent
    public static void alembicPre(AlembicDamageEvent.Pre event) {
    }

    @SubscribeEvent
    public static void alembicPost(AlembicDamageEvent.Post event) {
    }
}
