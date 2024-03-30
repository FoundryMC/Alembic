package foundry.alembic.tests;

import foundry.alembic.Alembic;
import foundry.alembic.ForgeEvents;
import foundry.alembic.tests.mixin.LivingEntityTestAccessor;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.Range;

//TODO: Potentially change to neo's framework in 1.21. I've seen some complaints abt it, so check it out first
@GameTestHolder(Alembic.MODID)
public class BasicDamageTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "9x9x9")
    public void playerHurtZombieTest(final GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, player.blockPosition().east());
        player.attack(zombie);
        float lastHurt = ((LivingEntityTestAccessor)zombie).getLastHurt();
        Range<Float> handHurtRange = Range.between(0.4f, 1.0f);
        helper.assertTrue(handHurtRange.contains(lastHurt), "Damage was not of expected amount. Was: %s".formatted(lastHurt));

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "9x9x9")
    public void iFramesTest(final GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, player.blockPosition().east());
        player.attack(zombie);
        float lastHurt = ((LivingEntityTestAccessor)zombie).getLastHurt();
        player.attack(zombie);
        lastHurt -= ((LivingEntityTestAccessor)zombie).getLastHurt();
        helper.assertTrue(lastHurt == 0, "Entity was damaged during invulnerability frames");

        helper.succeed();
    }
}
