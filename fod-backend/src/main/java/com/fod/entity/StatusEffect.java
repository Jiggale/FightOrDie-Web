package com.fod.entity;

import java.util.Objects;

/**
 * 状态效果实体 - Buff/Debuff 系统
 */
public class StatusEffect {

    public enum EffectType {
        BUFF, DEBUFF, DOT, HOT, STUN, SILENCE
    }

    private final String name;
    private final EffectType type;
    private final int duration;
    private int remainingTurns;
    private final int attackModifier;
    private final int defenseModifier;
    private final int tickDamage;
    private final int tickHeal;

    private StatusEffect(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.duration = builder.duration;
        this.remainingTurns = builder.duration;
        this.attackModifier = builder.attackModifier;
        this.defenseModifier = builder.defenseModifier;
        this.tickDamage = builder.tickDamage;
        this.tickHeal = builder.tickHeal;
    }

    public void tick() {
        if (remainingTurns > 0) remainingTurns--;
    }

    public boolean isExpired() { return remainingTurns <= 0; }

    public int getAttackModifier() { return attackModifier; }
    public int getDefenseModifier() { return defenseModifier; }
    public int getTickDamage() { return tickDamage; }
    public int getTickHeal() { return tickHeal; }
    public String getName() { return name; }
    public EffectType getType() { return type; }
    public int getDuration() { return duration; }
    public int getRemainingTurns() { return remainingTurns; }

    public static class Builder {
        private String name;
        private EffectType type = EffectType.BUFF;
        private int duration = 3;
        private int attackModifier;
        private int defenseModifier;
        private int tickDamage;
        private int tickHeal;

        public Builder name(String name) { this.name = name; return this; }
        public Builder type(EffectType type) { this.type = type; return this; }
        public Builder duration(int duration) { this.duration = duration; return this; }
        public Builder attackModifier(int mod) { this.attackModifier = mod; return this; }
        public Builder defenseModifier(int mod) { this.defenseModifier = mod; return this; }
        public Builder tickDamage(int dmg) { this.tickDamage = dmg; return this; }
        public Builder tickHeal(int heal) { this.tickHeal = heal; return this; }

        public StatusEffect build() {
            Objects.requireNonNull(name, "状态效果名不能为空");
            return new StatusEffect(this);
        }
    }
}
