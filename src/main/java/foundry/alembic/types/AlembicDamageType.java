package foundry.alembic.types;

import foundry.alembic.types.tags.AlembicTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

import java.util.ArrayList;
import java.util.List;


public class AlembicDamageType {
    private int priority;
    private ResourceLocation id;
    private double base;
    private double min;
    private double max;
    private boolean hasShielding;
    private boolean hasResistance;
    private boolean hasAbsorption;
    private boolean enableParticles;
    private AlembicAttribute attribute;
    private DamageSource damageSource;
    private int color;
    private List<AlembicTag<?, ?>> tags = new ArrayList<>();
    private AlembicAttribute shieldAttribute;
    private AlembicAttribute resistanceAttribute;
    private AlembicAttribute absorptionAttribute;

    public AlembicDamageType(int priority, ResourceLocation id, double base, double min, double max, boolean hasShielding, boolean hasResistance, boolean hasAbsorption, int color, boolean enableParticles) {
        this.priority = priority;
        this.id = id;
        this.base = base;
        this.min = min;
        this.max = max;
        this.hasShielding = hasShielding;
        this.hasResistance = hasResistance;
        this.hasAbsorption = hasAbsorption;
        this.attribute = new AlembicAttribute(id.toString(), base, min, max);
        this.damageSource = new DamageSource(id.toString());
        this.color = color;
        this.enableParticles = enableParticles;
        this.shieldAttribute = new AlembicAttribute(id.toString() + "_shield", 0, 0, 1024);
        this.resistanceAttribute = new AlembicAttribute(id.toString() + "_resistance", 0, -1024, 1024);
        this.absorptionAttribute = new AlembicAttribute(id.toString() + "_absorption", 0, 0, 1024);
    }

    public void addTag(AlembicTag<?, ?> tag) {
        if (tag.toString().equals("PARTICLE")) {
            System.out.println("WHY");
        }
        this.tags.add(tag);
    }

    public List<AlembicTag<?, ?>> getTags() {
        return this.tags;
    }

    public void clearTags() {
        this.tags.clear();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public ResourceLocation getId() {
        return id;
    }

    public double getBase() {
        return base;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean hasShielding() {
        return hasShielding;
    }

    public boolean hasResistance() {
        return hasResistance;
    }

    public boolean hasAbsorption() {
        return hasAbsorption;
    }

    public AlembicAttribute getAttribute() {
        return attribute;
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }

    public Component getVisualString() {
        return Component.literal("ID: " + id.toString() +
                        " Base: " + base + " Min: " + min +
                        " Max: " + max + " Shielding: " + hasShielding +
                        " Resistance: " + hasResistance +
                        " Absorption: " + hasAbsorption +
                        " Tags: " + tagString())
                .withStyle(s -> s.withColor(color));
    }

    public String tagString() {
        StringBuilder tagString = new StringBuilder();
        for (AlembicTag<?, ?> tag : tags) {
            tagString.append(tag.toString()).append(", ");
        }
        return tagString.toString();
    }

    public int getColor() {
        return color;
    }

    public boolean enableParticles() {
        return enableParticles;
    }

    public void setHasShielding(boolean hasShielding) {
        this.hasShielding = hasShielding;
    }

    public void setHasResistance(boolean hasResistance) {
        this.hasResistance = hasResistance;
    }

    public void setHasAbsorption(boolean hasAbsorption) {
        this.hasAbsorption = hasAbsorption;
    }

    public void setEnableParticles(boolean enableParticles) {
        this.enableParticles = enableParticles;
    }

    public void setBase(double base) {
        this.base = base;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setAttribute(AlembicAttribute attribute) {
        this.attribute = attribute;
    }

    public void setDamageSource(DamageSource damageSource) {
        this.damageSource = damageSource;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public AlembicAttribute getShieldAttribute() {
        return shieldAttribute;
    }

    public AlembicAttribute getResistanceAttribute() {
        return resistanceAttribute;
    }

    public AlembicAttribute getAbsorptionAttribute() {
        return absorptionAttribute;
    }

    public void setShieldAttribute(AlembicAttribute shieldAttribute) {
        this.shieldAttribute = shieldAttribute;
    }

    public void setResistanceAttribute(AlembicAttribute resistanceAttribute) {
        this.resistanceAttribute = resistanceAttribute;
    }

    public void setAbsorptionAttribute(AlembicAttribute absorptionAttribute) {
        this.absorptionAttribute = absorptionAttribute;
    }
}
