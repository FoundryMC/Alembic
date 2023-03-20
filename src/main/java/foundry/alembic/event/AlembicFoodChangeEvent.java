package foundry.alembic.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class AlembicFoodChangeEvent extends Event {
    Player player;
    int foodLevel;
    int lastFoodLevel;
    float saturationLevel;
    float exhaustionLevel;

    public AlembicFoodChangeEvent(Player player, int foodLevel, int lastFoodLevel, float saturationLevel, float exhaustionLevel) {
        this.player = player;
        this.foodLevel = foodLevel;
        this.lastFoodLevel = lastFoodLevel;
        this.saturationLevel = saturationLevel;
        this.exhaustionLevel = exhaustionLevel;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public int getLastFoodLevel() {
        return lastFoodLevel;
    }

    public void setLastFoodLevel(int lastFoodLevel) {
        this.lastFoodLevel = lastFoodLevel;
    }

    public float getSaturationLevel() {
        return saturationLevel;
    }

    public void setSaturationLevel(float saturationLevel) {
        this.saturationLevel = saturationLevel;
    }

    public float getExhaustionLevel() {
        return exhaustionLevel;
    }

    public void setExhaustionLevel(float exhaustionLevel) {
        this.exhaustionLevel = exhaustionLevel;
    }

    public Player getPlayer() {
        return player;
    }

    public static class Increase extends AlembicFoodChangeEvent {
        public Increase(Player player, int foodLevel, int lastFoodLevel, float saturationLevel, float exhaustionLevel) {
            super(player, foodLevel, lastFoodLevel, saturationLevel, exhaustionLevel);
        }
    }

    public static class Decrease extends AlembicFoodChangeEvent {
        public Decrease(Player player, int foodLevel, int lastFoodLevel, float saturationLevel, float exhaustionLevel) {
            super(player, foodLevel, lastFoodLevel, saturationLevel, exhaustionLevel);
        }
    }
}
