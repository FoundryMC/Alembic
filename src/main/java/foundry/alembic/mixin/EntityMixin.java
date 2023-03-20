package foundry.alembic.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import foundry.alembic.AlembicAPI;
import foundry.alembic.networking.AlembicPacketHandler;
import foundry.alembic.networking.ClientboundAlembicFireTypePacket;
import foundry.alembic.types.potion.AlembicFlammablePlayer;
import foundry.alembic.types.potion.AlembicPotionRegistry;
import foundry.alembic.types.potion.OverrideHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin implements AlembicFlammablePlayer {
    @Unique
    private String alembic$lastFireBlock = "";

    @Inject(method = "setRemainingFireTicks", at = @At("HEAD"))
    private void alembic$setRemainingFireTicks(int ticks, CallbackInfo info) {
        Entity entity = (Entity) (Object) this;
        if(ticks <= 2) return;
        if(entity instanceof LivingEntity le){
            OverrideHelper.addFireEffect(ticks, le, alembic$lastFireBlock);
        }
    }

    @Inject(method = "clearFire", at = @At("HEAD"))
    private void alembic$clearFire(CallbackInfo info) {
        Entity entity = (Entity) (Object) this;
        if(entity instanceof LivingEntity le){
            OverrideHelper.removeFireEffect(le);
        }
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void alembic$baseTick(CallbackInfo info) {
        Entity entity = (Entity) (Object) this;
        if(entity.getRemainingFireTicks() == 0 && !alembic$lastFireBlock.equals("")) {
            setAlembicLastFireBlock("");
        }
    }

    @WrapOperation(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 0))
    private boolean alembic$onEntityBaseTick(Entity instance, DamageSource pSource, float pAmount, Operation<Boolean> original) {
        if(alembic$lastFireBlock.equals("soul")){
            return instance.hurt(AlembicAPI.SOUL_FIRE, pAmount);
        } else {
            return original.call(instance, pSource, pAmount);
        }
    }

    @Override
    public String getAlembicLastFireBlock() {
        return alembic$lastFireBlock;
    }

    @Override
    public void setAlembicLastFireBlock(String block) {
        alembic$lastFireBlock = block;
        Entity entity = (Entity) (Object) this;
        if(entity instanceof ServerPlayer pl){
            AlembicPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (pl)), new ClientboundAlembicFireTypePacket(alembic$lastFireBlock));
        }
    }
}
