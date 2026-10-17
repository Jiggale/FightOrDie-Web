package com.fod.entity;

import java.util.Objects;
import java.util.UUID;

/**
 * 装备实体 - 装饰器模式实现属性叠加
 */
public class Equipment {

    public enum Slot {
        WEAPON, ARMOR, ACCESSORY
    }

    private final String id;
    private final String name;
    private final String description;
    private final Slot slot;
    private final Item.Rarity rarity;
    private final int attackBonus;
    private final int defenseBonus;
    private final int requiredLevel;
    private final int value;

    private Equipment(Builder builder) {
        this.id = UUID.randomUUID().toString();
        this.name = builder.name;
        this.description = builder.description;
        this.slot = builder.slot;
        this.rarity = builder.rarity;
        this.attackBonus = builder.attackBonus;
        this.defenseBonus = builder.defenseBonus;
        this.requiredLevel = builder.requiredLevel;
        this.value = builder.value;
    }

    public boolean canEquip(Player player) {
        return player.getLevel() >= requiredLevel;
    }

    public static class Builder {
        private String name;
        private String description;
        private Slot slot = Slot.WEAPON;
        private Item.Rarity rarity = Item.Rarity.COMMON;
        private int attackBonus;
        private int defenseBonus;
        private int requiredLevel = 1;
        private int value;

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String desc) { this.description = desc; return this; }
        public Builder slot(Slot slot) { this.slot = slot; return this; }
        public Builder rarity(Item.Rarity rarity) { this.rarity = rarity; return this; }
        public Builder attackBonus(int bonus) { this.attackBonus = bonus; return this; }
        public Builder defenseBonus(int bonus) { this.defenseBonus = bonus; return this; }
        public Builder requiredLevel(int lvl) { this.requiredLevel = lvl; return this; }
        public Builder value(int value) { this.value = value; return this; }

        public Equipment build() {
            Objects.requireNonNull(name, "装备名不能为空");
            return new Equipment(this);
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Slot getSlot() { return slot; }
    public Item.Rarity getRarity() { return rarity; }
    public int getAttackBonus() { return attackBonus; }
    public int getDefenseBonus() { return defenseBonus; }
    public int getRequiredLevel() { return requiredLevel; }
    public int getValue() { return value; }

    @Override
    public String toString() {
        return "[" + rarity + "] " + name + " (攻击+" + attackBonus + " 防御+" + defenseBonus + ")";
    }
}
