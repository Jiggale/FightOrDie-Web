package com.fod.entity;

import java.util.Objects;
import java.util.UUID;

/**
 * 物品实体 - 不可变对象
 * 使用享元模式（Flyweight）管理相同类型的物品实例
 */
public final class Item {

    public enum Rarity {
        COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
    }

    public enum ItemType {
        CONSUMABLE,   // 消耗品
        KEY_ITEM,     // 剧情道具
        MATERIAL,    // 材料
        QUEST_ITEM   // 任务道具
    }

    private final String id;
    private final String name;
    private final String description;
    private final Rarity rarity;
    private final ItemType type;
    private final int value;
    private final int healAmount;
    private final int attackBonus;
    private final int defenseBonus;
    private final boolean stackable;

    private Item(Builder builder) {
        this.id = UUID.randomUUID().toString();
        this.name = builder.name;
        this.description = builder.description;
        this.rarity = builder.rarity;
        this.type = builder.type;
        this.value = builder.value;
        this.healAmount = builder.healAmount;
        this.attackBonus = builder.attackBonus;
        this.defenseBonus = builder.defenseBonus;
        this.stackable = builder.stackable;
    }

    public static Item valueOf(String name, String description, Rarity rarity, ItemType type, int value) {
        return new Builder().name(name).description(description).rarity(rarity).type(type).value(value).build();
    }

    public static Item createPotion(String name, int healAmount, Rarity rarity, int value) {
        return new Builder()
                .name(name).description("恢复 " + healAmount + " 点生命值")
                .rarity(rarity).type(ItemType.CONSUMABLE).value(value)
                .healAmount(healAmount).stackable(true)
                .build();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Rarity getRarity() { return rarity; }
    public ItemType getType() { return type; }
    public int getValue() { return value; }
    public int getHealAmount() { return healAmount; }
    public int getAttackBonus() { return attackBonus; }
    public int getDefenseBonus() { return defenseBonus; }
    public boolean isStackable() { return stackable; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        return name.equals(item.name) && rarity == item.rarity && type == item.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rarity, type);
    }

    @Override
    public String toString() {
        return rarity.name() + " " + name;
    }

    public static class Builder {
        private String name;
        private String description;
        private Rarity rarity = Rarity.COMMON;
        private ItemType type = ItemType.MATERIAL;
        private int value;
        private int healAmount;
        private int attackBonus;
        private int defenseBonus;
        private boolean stackable;

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder rarity(Rarity rarity) { this.rarity = rarity; return this; }
        public Builder type(ItemType type) { this.type = type; return this; }
        public Builder value(int value) { this.value = value; return this; }
        public Builder healAmount(int amount) { this.healAmount = amount; return this; }
        public Builder attackBonus(int bonus) { this.attackBonus = bonus; return this; }
        public Builder defenseBonus(int bonus) { this.defenseBonus = bonus; return this; }
        public Builder stackable(boolean stackable) { this.stackable = stackable; return this; }

        public Item build() {
            Objects.requireNonNull(name, "物品名不能为空");
            return new Item(this);
        }
    }
}
