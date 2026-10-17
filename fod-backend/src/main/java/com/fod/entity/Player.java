package com.fod.entity;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 玩家实体 - 建造者模式构建复杂角色
 */
public class Player {

    private final String id;
    private final String name;
    private int health;
    private int maxHealth;
    private int attack;
    private int defense;
    private int level;
    private int experience;
    private int gold;

    private Equipment weapon;
    private Equipment armor;
    private Equipment accessory;

    private final List<Item> inventory;
    private final List<String> quests;
    private final Map<String, Integer> killCount;
    private final List<StatusEffect> activeEffects;

    private Player(Builder builder) {
        this.id = UUID.randomUUID().toString();
        this.name = builder.name;
        this.health = builder.health;
        this.maxHealth = builder.maxHealth;
        this.attack = builder.attack;
        this.defense = builder.defense;
        this.level = builder.level;
        this.experience = 0;
        this.gold = 0;
        this.inventory = new CopyOnWriteArrayList<>();
        this.quests = new ArrayList<>();
        this.killCount = new HashMap<>();
        this.activeEffects = new ArrayList<>();
    }

    public static class Builder {
        private String name;
        private int health = 100;
        private int maxHealth = 100;
        private int attack = 10;
        private int defense = 5;
        private int level = 1;

        public Builder name(String name) { this.name = name; return this; }
        public Builder health(int health) { this.health = health; return this; }
        public Builder attack(int attack) { this.attack = attack; return this; }
        public Builder defense(int defense) { this.defense = defense; return this; }
        public Builder level(int level) { this.level = level; return this; }

        public Player build() {
            Objects.requireNonNull(name, "角色名不能为空");
            return new Player(this);
        }
    }

    public boolean isAlive() { return health > 0; }

    public void takeDamage(int damage) {
        int actualDamage = Math.max(damage - getEffectiveDefense(), 0);
        health = Math.max(health - actualDamage, 0);
    }

    public void heal(int amount) { health = Math.min(health + amount, maxHealth); }

    public void gainExperience(int exp) {
        this.experience += exp;
        while (experience >= getExpForNextLevel()) levelUp();
    }

    private int getExpForNextLevel() { return level * 100; }

    private void levelUp() {
        level++;
        maxHealth += 20;
        health = maxHealth;
        attack += 5;
        defense += 3;
    }

    public int getEffectiveAttack() {
        int base = attack;
        if (weapon != null) base += weapon.getAttackBonus();
        if (armor != null) base += armor.getAttackBonus();
        if (accessory != null) base += accessory.getAttackBonus();
        for (StatusEffect effect : activeEffects) base += effect.getAttackModifier();
        return Math.max(base, 1);
    }

    public int getEffectiveDefense() {
        int base = defense;
        if (weapon != null) base += weapon.getDefenseBonus();
        if (armor != null) base += armor.getDefenseBonus();
        if (accessory != null) base += accessory.getDefenseBonus();
        for (StatusEffect effect : activeEffects) base += effect.getDefenseModifier();
        return Math.max(base, 0);
    }

    public boolean addItem(Item item) { return inventory.size() < 20 && inventory.add(item); }
    public boolean removeItem(Item item) { return inventory.remove(item); }
    public boolean hasItem(Item item) { return inventory.contains(item); }

    public void recordKill(String monsterName) { killCount.merge(monsterName, 1, Integer::sum); }
    public int getKillCount(String monsterName) { return killCount.getOrDefault(monsterName, 0); }
    public int getTotalKills() { return killCount.values().stream().mapToInt(Integer::intValue).sum(); }

    public void addEffect(StatusEffect effect) { activeEffects.add(effect); }
    public void tickEffects() { activeEffects.removeIf(effect -> { effect.tick(); return effect.isExpired(); }); }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getGold() { return gold; }
    public List<Item> getInventory() { return inventory; }
    public List<String> getQuests() { return quests; }
    public Map<String, Integer> getKillCount() { return killCount; }

    public void setHealth(int health) { this.health = Math.min(health, maxHealth); }
    public void setGold(int gold) { this.gold = Math.max(gold, 0); }
    public void addGold(int amount) { this.gold += amount; }
    public void spendGold(int amount) { this.gold = Math.max(gold - amount, 0); }

    public Equipment getWeapon() { return weapon; }
    public Equipment getArmor() { return armor; }
    public Equipment getAccessory() { return accessory; }

    public void equip(Equipment equipment) {
        switch (equipment.getSlot()) {
            case WEAPON -> this.weapon = equipment;
            case ARMOR -> this.armor = equipment;
            case ACCESSORY -> this.accessory = equipment;
        }
    }

    public void addQuest(String quest) { this.quests.add(quest); }
    public void completeQuest(String quest) { this.quests.remove(quest); }

    @Override
    public String toString() {
        return String.format("Player{name='%s', level=%d, hp=%d/%d, atk=%d, def=%d}",
                name, level, health, maxHealth, getEffectiveAttack(), getEffectiveDefense());
    }
}
