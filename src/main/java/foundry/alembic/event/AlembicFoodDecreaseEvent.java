package foundry.alembic.event;

import net.minecraftforge.eventbus.api.Event;

public class AlembicFoodDecreaseEvent extends Event {
    int foodLevel;
    int lastFoodLevel;
    float saturationLevel;
    float exhaustionLevel;

    public AlembicFoodDecreaseEvent(int foodLevel, int lastFoodLevel, float saturationLevel, float exhaustionLevel) {
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
}
