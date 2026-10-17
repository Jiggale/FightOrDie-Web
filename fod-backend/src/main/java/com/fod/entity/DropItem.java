package com.fod.entity;

/**
 * 怪物掉落物品条目
 */
public class DropItem {

    private final Object item;
    private final double chance;
    private final String category;

    public DropItem(Object item, double chance) {
        this.item = item;
        this.chance = Math.min(Math.max(chance, 0.0), 1.0);
        this.category = item instanceof Equipment ? "equipment" : "item";
    }

    public Item asItem() {
        return item instanceof Item ? (Item) item : null;
    }

    public Equipment asEquipment() {
        return item instanceof Equipment ? (Equipment) item : null;
    }

    public Object getRaw() { return item; }
    public String getCategory() { return category; }
    public double getChance() { return chance; }

    public String getDisplayName() {
        if (item instanceof Item) return ((Item) item).getName();
        if (item instanceof Equipment) return ((Equipment) item).getName();
        return "未知物品";
    }
}
