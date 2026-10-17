package com.fod.entity;

import java.util.Objects;
import java.util.UUID;

/**
 * 技能实体
 */
public class Skill {

    public enum SkillType { ACTIVE, PASSIVE, ULTIMATE }
    public enum TargetType { SELF, SINGLE, ALLY, ENEMY }

    private final String id;
    private final String name;
    private final String description;
    private final SkillType type;
    private final TargetType targetType;
    private final int manaCost;
    private final int cooldown;
    private final int currentCooldown;
    private final double damageMultiplier;
    private final int fixedDamage;
    private final int healAmount;
    private final StatusEffect appliedEffect;
    private final int requiredLevel;

    private Skill(Builder builder) {
        this.id = UUID.randomUUID().toString();
        this.name = builder.name;
        this.description = builder.description;
        this.type = builder.type;
        this.targetType = builder.targetType;
        this.manaCost = builder.manaCost;
        this.cooldown = builder.cooldown;
        this.currentCooldown = 0;
        this.damageMultiplier = builder.damageMultiplier;
        this.fixedDamage = builder.fixedDamage;
        this.healAmount = builder.healAmount;
        this.appliedEffect = builder.appliedEffect;
        this.requiredLevel = builder.requiredLevel;
    }

    public boolean isReady() { return currentCooldown <= 0; }
    public boolean canUse(Player player) { return player.getLevel() >= requiredLevel && isReady(); }

    public static class Builder {
        private String name;
        private String description;
        private SkillType type = SkillType.ACTIVE;
        private TargetType targetType = TargetType.ENEMY;
        private int manaCost;
        private int cooldown;
        private double damageMultiplier = 1.0;
        private int fixedDamage;
        private int healAmount;
        private StatusEffect appliedEffect;
        private int requiredLevel = 1;

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String desc) { this.description = desc; return this; }
        public Builder type(SkillType type) { this.type = type; return this; }
        public Builder targetType(TargetType tt) { this.targetType = tt; return this; }
        public Builder manaCost(int cost) { this.manaCost = cost; return this; }
        public Builder cooldown(int cd) { this.cooldown = cd; return this; }
        public Builder damageMultiplier(double mult) { this.damageMultiplier = mult; return this; }
        public Builder fixedDamage(int dmg) { this.fixedDamage = dmg; return this; }
        public Builder healAmount(int heal) { this.healAmount = heal; return this; }
        public Builder appliedEffect(StatusEffect effect) { this.appliedEffect = effect; return this; }
        public Builder requiredLevel(int lvl) { this.requiredLevel = lvl; return this; }

        public Skill build() {
            Objects.requireNonNull(name, "技能名不能为空");
            return new Skill(this);
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SkillType getType() { return type; }
    public TargetType getTargetType() { return targetType; }
    public int getManaCost() { return manaCost; }
    public int getCooldown() { return cooldown; }
    public int getCurrentCooldown() { return currentCooldown; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public int getFixedDamage() { return fixedDamage; }
    public int getHealAmount() { return healAmount; }
    public StatusEffect getAppliedEffect() { return appliedEffect; }
    public int getRequiredLevel() { return requiredLevel; }
}
