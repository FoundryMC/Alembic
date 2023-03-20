package foundry.alembic;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import foundry.alembic.client.TooltipHelper;
import foundry.alembic.damagesource.AlembicDamageSourceIdentifier;
import foundry.alembic.event.AlembicDamageDataModificationEvent;
import foundry.alembic.event.AlembicDamageEvent;
import foundry.alembic.items.ItemStatHolder;
import foundry.alembic.items.ItemStatJSONListener;
import foundry.alembic.networking.AlembicPacketHandler;
import foundry.alembic.networking.ClientboundAlembicDamagePacket;
import foundry.alembic.override.AlembicOverride;
import foundry.alembic.override.AlembicOverrideHolder;
import foundry.alembic.override.OverrideJSONListener;
import foundry.alembic.resistances.AlembicResistance;
import foundry.alembic.resistances.AlembicResistanceHolder;
import foundry.alembic.resistances.ResistanceJsonListener;
import foundry.alembic.types.AlembicDamageType;
import foundry.alembic.types.DamageTypeJSONListener;
import foundry.alembic.types.DamageTypeRegistry;
import foundry.alembic.types.potion.AlembicPotionDataHolder;
import foundry.alembic.types.potion.AlembicPotionRegistry;
import foundry.alembic.types.tags.AlembicGlobalTagPropertyHolder;
import foundry.alembic.types.tags.AlembicHungerTag;
import foundry.alembic.types.tags.AlembicPerLevelTag;
import foundry.alembic.types.tags.AlembicTag;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static foundry.alembic.Alembic.MODID;
import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    public static UUID ALEMBIC_FIRE_RESIST_UUID = UUID.fromString("b3f2b2f0-2b8a-4b9b-9b9b-2b8a4b9b9b9b");
    @SubscribeEvent
    static void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
    }

    @SubscribeEvent
    static void onLevelUp(PlayerXpEvent.LevelChange event){
        Player player = event.getEntity();
        if(player.level.isClientSide) return;
        for (AlembicPerLevelTag tag : AlembicGlobalTagPropertyHolder.getLevelupBonuses(event.getEntity())) {
            RangedAttribute attribute = tag.getAffectedType();
            if (player.getAttributes().hasAttribute(attribute)) {
                AttributeInstance playerAttribute = player.getAttribute(attribute);
                playerAttribute.setBaseValue(playerAttribute.getBaseValue()+tag.getBonusPerLevel());
                player.sendSystemMessage(Component.literal("You have leveled up your %s to %s".formatted(attribute.descriptionId, playerAttribute.getValue())).withStyle(style -> style.withColor(ChatFormatting.GOLD)));

                CompoundTag nbt = player.getPersistentData();
                if (nbt.contains(attribute.descriptionId)) {
                    if (nbt.getInt(attribute.descriptionId) < tag.getCap()) {
                        nbt.putInt(attribute.descriptionId, nbt.getInt(attribute.descriptionId)+1);
                    }
                } else {
                    nbt.putInt(attribute.descriptionId, 1);
                }
            }
        }
    }

    @SubscribeEvent
    static void onRightClickTest(PlayerInteractEvent.RightClickItem event){
        if(event.getEntity().level.isClientSide) return;
    }

    @SubscribeEvent
    static void onJsonListener(AddReloadListenerEvent event){
        DamageTypeJSONListener.register(event);
        OverrideJSONListener.register(event);
        ResistanceJsonListener.register(event);
        ItemStatJSONListener.register(event);
    }

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().level.isClientSide) return;
        if (event.getItemStack().getItem() == Items.STICK) {
            for (AlembicDamageType damageType : DamageTypeRegistry.getDamageTypes()) {
                event.getEntity().sendSystemMessage(damageType.getVisualString());
                event.getEntity().sendSystemMessage(Component.literal(event.getEntity().getAttribute(damageType.getAttribute()).getValue() + "").withStyle(s -> s.withColor(ChatFormatting.GOLD)));
            }
        }
    }

    @SubscribeEvent
    public static void onTooltipRender(ItemTooltipEvent event){
        int target = 0;
        List<Component> toRemove = new ArrayList<>();
        for(Component component : event.getToolTip()){
            if(component.getString().contains("When in")){
                target = event.getToolTip().indexOf(component) + 1;
            }
            if(component.toString().contains("alembic") && (event.getItemStack().getItem() instanceof SwordItem || event.getItemStack().getItem() instanceof TridentItem || event.getItemStack().getItem() instanceof DiggerItem)){
                toRemove.add(component);
            }
        }
        event.getToolTip().removeAll(toRemove);
        if(target != 0){
            int finalTarget = target;
            List<Pair<Attribute, AttributeModifier>> holder = ItemStatHolder.get(event.getItemStack().getItem());
            if(holder == null) return;
            holder.forEach(pair -> {
                if(event.getEntity() == null) return;
                if(pair.getFirst().descriptionId.contains("physical_damage")) return;
                double d0 = pair.getSecond().getAmount();
                d0 += event.getEntity().getAttributeBaseValue(pair.getFirst());
                d0 = TooltipHelper.getMod(pair.getSecond(), d0);
                event.getToolTip().add(finalTarget, Component.literal(" ").append(Component.translatable("attribute.modifier.equals." + pair.getSecond().getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d0), Component.translatable(pair.getFirst().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
            });
        }

    }

    @SubscribeEvent
    public static void onItemAttributes(ItemAttributeModifierEvent event){
        List<Pair<Attribute, AttributeModifier>> holder = ItemStatHolder.get(event.getItemStack().getItem());
        if(holder == null) return;
        holder.forEach(am -> {
            if(event.getSlotType().name().equals(ItemStatJSONListener.getStat(event.getItemStack().getItem()).equipmentSlot())){
                //event.addModifier(am.getFirst(), am.getSecond());
                if(am.getFirst().equals(ForgeRegistries.ATTRIBUTES.getValue(Alembic.location("physical_damage")))){
                    AtomicReference<Pair<Attribute, AttributeModifier>> toRemove = new AtomicReference<>();
                    event.getOriginalModifiers().forEach((attribute, attributeModifier) -> {
                        if(attribute.equals(Attributes.ATTACK_DAMAGE)){
                            toRemove.set(Pair.of(attribute, attributeModifier));
                        }
                    });
                    if(toRemove.get() != null) event.getOriginalModifiers().remove(toRemove.get().getFirst(), toRemove.get().getSecond());
                    event.getOriginalModifiers().put(Attributes.ATTACK_DAMAGE, am.getSecond());
                } else {
                    event.getOriginalModifiers().put(am.getFirst(), am.getSecond());
                }
            }
        });
    }


    @SubscribeEvent
    public static void onLivingSpawn(final LivingSpawnEvent event){
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void cancelShieldBlock(ShieldBlockEvent event){
        event.setBlockedDamage(0);
    }

    private static boolean noRecurse = false;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void hurt(final LivingHurtEvent e){
        if (e.getEntity().level.isClientSide) return;
        if (noRecurse) return;
        noRecurse = true;
        if(!e.getEntity().getActiveEffects().isEmpty()){
            for(MobEffectInstance effectInstances : e.getEntity().getActiveEffects()){
                ResourceLocation rl = ForgeRegistries.MOB_EFFECTS.getKey(effectInstances.getEffect());
                if(rl != null){
                    if(AlembicPotionRegistry.IMMUNITY_DATA.containsKey(rl)){
                        AlembicPotionDataHolder data = AlembicPotionRegistry.IMMUNITY_DATA.get(rl);
                        if(data != null){
                            if (data.getImmunities().stream().map(AlembicDamageSourceIdentifier::getSerializedName).toList().contains(e.getSource().msgId)){
                                e.setCanceled(true);
                                noRecurse = false;
                                return;
                            }
                        }
                    }
                }
            }
        }
        if (e.getSource().getDirectEntity() == null || e.getSource().getDirectEntity() instanceof AbstractArrow) {
            noRecurse = false;
            LivingEntity target = e.getEntity();
            float totalDamage = e.getAmount();
            AlembicOverride override = AlembicOverrideHolder.getOverridesForSource(e.getSource());
            if (override != null) {
                handleTypedDamage(target, null, totalDamage, override, e.getSource());
                //target.hurt(e.getSource(), totalDamage);
                noRecurse = false;
                e.setCanceled(true);
            }
        }
        if (e.getSource().getDirectEntity() instanceof LivingEntity || e.getSource().getDirectEntity() instanceof AbstractHurtingProjectile) {
            LivingEntity attacker;
            if (e.getSource().getDirectEntity() instanceof LivingEntity living) {
                attacker = living;
            } else {
                AbstractHurtingProjectile projectile = (AbstractHurtingProjectile) e.getSource().getDirectEntity();
                attacker = (LivingEntity) projectile.getOwner();
            }
            LivingEntity target = e.getEntity();
            float totalDamage = e.getAmount();
            AlembicResistance stats = AlembicResistanceHolder.get(attacker.getType());
            boolean entityOverride = stats != null;
            float damageOffset = 0;
            if (entityOverride) {
                damageOffset = handleTypedDamage(target, attacker, totalDamage, stats, e.getSource());
            }
            totalDamage -= damageOffset;
            for (AlembicDamageType damageType : DamageTypeRegistry.getDamageTypes()) {
                if (attacker.getAttribute(damageType.getAttribute()).getValue() > 0) {
                    float damage = (float) attacker.getAttribute(damageType.getAttribute()).getValue();
                    if(target.isBlocking() && !damageType.getId().getPath().contains("true_damage")) {
                        damage *= 0.25;
                    }
                    float attrValue = target.getAttributes().hasAttribute(damageType.getResistanceAttribute()) ? (float) target.getAttribute(damageType.getResistanceAttribute()).getValue() : 0;
                    if (damageType.getId().equals(Alembic.location("physical_damage")))
                        attrValue = target.getArmorValue();
                    AlembicDamageEvent.Pre preDamage = new AlembicDamageEvent.Pre(target, attacker, damageType, damage, attrValue);
                    MinecraftForge.EVENT_BUS.post(preDamage);
                    damage = preDamage.getDamage();
                    attrValue = preDamage.getResistance();
                    if (damage <= 0 || preDamage.isCanceled()) return;
                    damage = CombatRules.getDamageAfterAbsorb(damage, attrValue, (float) target.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue());

                    handleDamageInstance(target, damageType, damage, e.getSource());
                    AlembicDamageEvent.Post postDamage = new AlembicDamageEvent.Post(target, attacker, damageType, damage, attrValue);
                    MinecraftForge.EVENT_BUS.post(postDamage);
                }
            }
            int time = target.invulnerableTime;
            target.invulnerableTime = 0;
            if (totalDamage > 0.001) {
                target.hurt(src(attacker), totalDamage);
            }
            target.invulnerableTime = time;
        }
        noRecurse = false;
        e.setCanceled(true);
    }

    private static void handleDamageInstance(LivingEntity target, AlembicDamageType damageType, float damage, DamageSource originalSource) {
        if (damage > 0) {
            damageType.getTags().forEach(r -> {
                AlembicTag.ComposedData data = AlembicTag.ComposedData.createEmpty()
                                .add(AlembicTag.ComposedDataType.LEVEL, target.level)
                                        .add(AlembicTag.ComposedDataType.TARGET_ENTITY, target)
                                        .add(AlembicTag.ComposedDataType.FINAL_DAMAGE, damage)
                                                .add(AlembicTag.ComposedDataType.ORIGINAL_SOURCE, originalSource);
                AlembicDamageDataModificationEvent event = new AlembicDamageDataModificationEvent(data);
                MinecraftForge.EVENT_BUS.post(event);
                data = event.getData();
                r.run(data);
            });
            int invtime = target.invulnerableTime;
            target.invulnerableTime = 0;
            float f2 = Math.max(damage - target.getAbsorptionAmount(), 0.0F);
            target.setAbsorptionAmount(target.getAbsorptionAmount() - (damage - f2));
            float f = damage - f2;
            if (f > 0.0F && f < 3.4028235E37F && target instanceof Player) {
                ((Player) target).awardStat(Stats.CUSTOM.get(Stats.DAMAGE_DEALT_ABSORBED), Math.round(f * 10.0F));
            }
            float health = target.getHealth();
            target.getCombatTracker().recordDamage(DamageSource.GENERIC, health, damage);
            target.setHealth(health - damage);
            target.setAbsorptionAmount(target.getAbsorptionAmount() - f2);
            AlembicPacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(() ->
                            new PacketDistributor.TargetPoint(target.getX(), target.getY(), target.getZ(), 128, target.level.dimension())),
                    new ClientboundAlembicDamagePacket(target.getId(), damageType.getId().toString(), damage, damageType.getColor()));
            target.gameEvent(GameEvent.ENTITY_DAMAGE);
            target.invulnerableTime = invtime;
        }
    }

    @SubscribeEvent
    static void onEffect(MobEffectEvent event){
        if(event.getEffectInstance() == null)return;
        AttributeModifier attmod = new AttributeModifier(ALEMBIC_FIRE_RESIST_UUID, "Fire Resistance", 0.1 *(1+ event.getEffectInstance().getAmplifier()), AttributeModifier.Operation.MULTIPLY_TOTAL);
        if(event instanceof MobEffectEvent.Added){
            if(event.getEffectInstance().getEffect().equals(MobEffects.FIRE_RESISTANCE)){
                Attribute fireRes = DamageTypeRegistry.getDamageType("fire_damage").getResistanceAttribute();
                if(fireRes == null) return;
                if(event.getEntity().getAttribute(fireRes) == null) return;
                Objects.requireNonNull(event.getEntity().getAttribute(fireRes)).addPermanentModifier(attmod);
            }
        }
        if (event instanceof MobEffectEvent.Remove){
            if(event.getEffectInstance() == null)return;
            if(event.getEffectInstance().getEffect().equals(MobEffects.FIRE_RESISTANCE)){
                Attribute fireRes = DamageTypeRegistry.getDamageType("fire_damage").getResistanceAttribute();
                if(fireRes == null) return;
                if(event.getEntity().getAttribute(fireRes) == null) return;
                event.getEntity().getAttribute(fireRes).removeModifier(attmod);
            }
        }
    }

    private static void handleTypedDamage(LivingEntity target, LivingEntity attacker, float totalDamage, AlembicOverride override, DamageSource originalSource) {
        for (Pair<AlembicDamageType, Float> pair : override.getDamages()) {
            AlembicDamageType damageType = pair.getFirst();
            float percentage = pair.getSecond();
            float damage = totalDamage * percentage;
            totalDamage -= damage;
            if (damage <= 0) {
                Alembic.LOGGER.warn("Damage overrides are too high! Damage is being reduced to 0 for {}!", damageType.getId().toString());
                continue;
            }
            damageCalc(target, attacker, damageType, damage, originalSource);
        }
    }

    private static float handleTypedDamage(LivingEntity target, LivingEntity attacker, float totalDamage, AlembicResistance stats, DamageSource originalSource) {
        AtomicReference<Float> total = new AtomicReference<>(0f);
        stats.getDamage().forEach((alembicDamageType, multiplier) -> {
            if (stats.getIgnoredSources().stream().map(AlembicDamageSourceIdentifier::getSerializedName).toList().contains(originalSource.msgId)) return;
            float damage = totalDamage * multiplier;
            if(target.isBlocking() && !alembicDamageType.getId().getPath().contains("true_damage")) {
                damage *= 0.25;
            }
            if (damage <= 0) {
                Alembic.LOGGER.warn("Damage overrides are too high! Damage is being reduced to 0 for {}!", alembicDamageType.getId().toString());
                return;
            }
            total.set(total.get() + damage);
            damageCalc(target, attacker, alembicDamageType, damage, originalSource);
        });
        return total.get();
    }

    private static void damageCalc(LivingEntity target, LivingEntity attacker, AlembicDamageType alembicDamageType, float damage, DamageSource originalSource) {
        float attrValue = target.getAttributes().hasAttribute(alembicDamageType.getResistanceAttribute()) ? (float) target.getAttribute(alembicDamageType.getResistanceAttribute()).getValue() : 0;
        if (alembicDamageType.getId().equals(Alembic.location("physical_damage"))) attrValue = target.getArmorValue();
        AlembicDamageEvent.Pre preDamage = new AlembicDamageEvent.Pre(target, attacker, alembicDamageType, damage, attrValue);
        MinecraftForge.EVENT_BUS.post(preDamage);
        damage = preDamage.getDamage();
        attrValue = preDamage.getResistance();
        if (damage <= 0 || preDamage.isCanceled()) return;
        target.hurtArmor(originalSource, damage);
        damage = CombatRules.getDamageAfterAbsorb(damage, attrValue, (float) target.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue());
        damage = AlembicDamageHelper.getDamageAfterAttributeAbsorb(target, alembicDamageType, damage);
        boolean enchantReduce = true;
        if(enchantReduce){
            int k = EnchantmentHelper.getDamageProtection(target.getArmorSlots(), DamageSource.mobAttack(attacker));
            if (k > 0) {
                damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float)k);
            }
        }
        float absorptionValue = target.getAttributes().hasAttribute(alembicDamageType.getAbsorptionAttribute()) ? (float) target.getAttribute(alembicDamageType.getAbsorptionAttribute()).getValue() : 0;
        if (absorptionValue > 0) {
            float absorption = Math.min(absorptionValue, damage);
            absorptionValue -= absorption;
            absorptionValue = Math.max(0, absorptionValue);
            target.sendSystemMessage(Component.literal("Absorbed " + absorption + " damage!").withStyle(s -> s.withColor(alembicDamageType.getColor())));
            damage -= absorption;
            if (alembicDamageType.hasAbsorption()) {
                target.getAttribute(alembicDamageType.getAbsorptionAttribute()).setBaseValue(absorptionValue);
            }
        }
        handleDamageInstance(target, alembicDamageType, damage, originalSource);
        AlembicDamageEvent.Post postDamage = new AlembicDamageEvent.Post(target, attacker, alembicDamageType, damage, attrValue);
        MinecraftForge.EVENT_BUS.post(postDamage);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void attack(final LivingAttackEvent e) {
    }

    private static DamageSource src(LivingEntity entity) {
        return entity instanceof Player p ? DamageSource.playerAttack(p) : DamageSource.mobAttack(entity);
    }

    @SubscribeEvent
    static void playerTick(TickEvent.PlayerTickEvent event){
        if(event.player.level.isClientSide) return;
        for(Map.Entry<AlembicDamageType, AlembicHungerTag> set : AlembicGlobalTagPropertyHolder.getHungerBonuses().entrySet()){
            AlembicDamageType type = set.getKey();
            AlembicHungerTag tag = set.getValue();
            Player player = event.player;
            //if(player.getFoodData().getFoodLevel() >= 20) return;
            RangedAttribute att = type.getAttribute(tag.getAttribute());
            if(AlembicDamageHelper.checkAttributeFromString(player, att.descriptionId)){
                AttributeInstance attribute = player.getAttribute(att);
                int hungerValue = player.getFoodData().getFoodLevel();
                float scalar = ((20 - hungerValue) % tag.getHungerTrigger() == 0) ? tag.getScaleAmount() : 1 ;
                AttributeModifier modifier = new AttributeModifier(tag.getUUID(), type.getId().getPath() + tag.getAttribute() + "_hunger_mod", scalar, tag.getOperation());
                if(attribute == null) continue;
                if(attribute.getModifier(tag.getUUID()) != null){
                    attribute.removeModifier(tag.getUUID());
                    attribute.addTransientModifier(modifier);
                } else {
                    attribute.addTransientModifier(modifier);
                }
                player.displayClientMessage(Component.literal("Resistance: " + attribute.getValue()), true);
            }
        }
    }


}