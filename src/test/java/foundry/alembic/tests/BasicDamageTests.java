package foundry.alembic.tests;

import foundry.alembic.Alembic;
import foundry.alembic.override.OverrideManager;
import foundry.alembic.tests.mixin.LivingEntityTestAccessor;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.Range;

//TODO: Potentially change to neo's framework in 1.21. I've seen some complaints abt it, so check it out first
@GameTestHolder(Alembic.MODID)
public class BasicDamageTests {

    // Ensures that a player hurts a zombie within a reasonable and expected damage range.
    @PrefixGameTestTemplate(false)
    @GameTest(template = "9x9x9")
    public void playerHurtZombieTest(final GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, player.blockPosition().east());
        player.attack(zombie);
        float lastHurt = ((LivingEntityTestAccessor)zombie).getLastHurt();
        Range<Float> handHurtRange = Range.between(0.2f, 0.8f);
        helper.assertTrue(handHurtRange.contains(lastHurt), "Damage was not of expected amount. Was: %s".formatted(lastHurt));

        helper.succeed();
    }

    // Ensures that I-frames work by hurting a zombie, capturing how much it was hurt by, attempting to hurt it again,
    // and then effectively comparing the amount of damage to see if it was the same amount of damage (which it should
    // be if it wasn't hurt again).
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

    // There was an issue that resulted in anything that wasn't mapped to an Alembic damage type would not deal damage.
    // This test uses an unmapped damage type and hurts a zombie with it to make sure that it does work.
    @PrefixGameTestTemplate(false)
    @GameTest(template = "9x9x9")
    public void killTest(final GameTestHelper helper) {
        DamageSource source = helper.getLevel().damageSources().genericKill();
        helper.assertTrue(!OverrideManager.containsKey(source), "%s has an override, change this test to a damage type without an override".formatted(source.typeHolder().unwrapKey().get().location()));

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, helper.relativePos(BlockPos.ZERO));

        zombie.hurt(source, Float.MAX_VALUE);

        helper.assertTrue(zombie.isDeadOrDying(), "Zombie isn't dying from exceedingly high damage!");

        helper.succeed();
    }

    // Tests that ShieldStats work correctly. Currently only ensures vanilla-like behavior for physical_damage being fully blocked.
    @PrefixGameTestTemplate(false)
    @GameTest(template = "9x9x9")
    public void shieldTest(final GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, player.blockPosition().east());
        player.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.getEyePosition());

        float playerHealth = player.getHealth();

        helper.startSequence()
                .thenExecute(() -> {
                    player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SHIELD));
                    player.startUsingItem(InteractionHand.MAIN_HAND);
                })
                .thenExecuteAfter(2, () -> zombie.doHurtTarget(player));

        helper.assertTrue(player.getHealth() == playerHealth, "Player took damage while using shield!");

        helper.succeed();
    }

    // TODO: Finish test. This should test that a custom `test_damage` damage type runs on a zombie being hurt
    @PrefixGameTestTemplate(false)
    @GameTest(template = "9x9x9")
    public void testTestDamage(final GameTestHelper helper) {
        DamageSource source = helper.getLevel().damageSources().wither();

        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, helper.relativePos(BlockPos.ZERO));

        zombie.hurt(source, 1);

//        helper.assertTrue(zombie.isDeadOrDying(), "Zombie isn't dying from exceedingly high damage!");

        helper.succeed();
    }
}
