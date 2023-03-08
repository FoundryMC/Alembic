package foundry.alembic.event;

import net.minecraftforge.eventbus.api.Event;

public class AlembicFoodChangeEvent extends Event {
    int foodLevel;
    int lastFoodLevel;
    float saturationLevel;
    float exhaustionLevel;

    public AlembicFoodChangeEvent(int foodLevel, int lastFoodLevel, float saturationLevel, float exhaustionLevel) {
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

    public static class Increase extends AlembicFoodChangeEvent {
        public Increase(int foodLevel, int lastFoodLevel, float saturationLevel, float exhaustionLevel) {
            super(foodLevel, lastFoodLevel, saturationLevel, exhaustionLevel);
        }
    }

    public static class Decrease extends AlembicFoodChangeEvent {
        public Decrease(int foodLevel, int lastFoodLevel, float saturationLevel, float exhaustionLevel) {
            super(foodLevel, lastFoodLevel, saturationLevel, exhaustionLevel);
        }
    }
}
