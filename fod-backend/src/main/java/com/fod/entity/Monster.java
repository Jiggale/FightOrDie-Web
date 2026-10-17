package com.fod.entity;

import java.util.*;

/**
 * 怪物实体 - 支持等级缩放、AI行为模式和掉落表
 */
public class Monster {

    public enum AIType {
        AGGRESSIVE, DEFENSIVE, GUARD, BOSS
    }

    private final String id;
    private final String name;
    private final int baseHealth;
    private final int baseAttack;
    private final int baseDefense;
    private final int level;
    private int health;
    private int attack;
    private int defense;
    private final int experienceReward;
    private final int goldReward;
    private final List<DropItem> dropTable;
    private final AIType aiType;
    private final Set<String> tags;

    private Monster(Builder builder) {
        this.id = UUID.randomUUID().toString();
        this.name = builder.name;
        this.baseHealth = builder.health;
        this.baseAttack = builder.attack;
        this.baseDefense = builder.defense;
        this.level = builder.level;
        this.health = builder.health;
        this.attack = builder.attack;
        this.defense = builder.defense;
        this.experienceReward = builder.exp;
        this.goldReward = builder.gold;
        this.dropTable = builder.dropTable != null ? builder.dropTable : new ArrayList<>();
        this.aiType = builder.aiType;
        this.tags = builder.tags != null ? builder.tags : new HashSet<>();
    }

    public void scaleToPlayerLevel(int playerLevel) {
        double ratio = (double) playerLevel / this.level;
        this.health = (int) (baseHealth * ratio);
        this.attack = (int) (baseAttack * ratio);
        this.defense = (int) (baseDefense * ratio);
    }

    public static class Builder {
        private String name;
        private int health = 30;
        private int attack = 5;
        private int defense = 2;
        private int level = 1;
        private int exp = 20;
        private int gold = 10;
        private List<DropItem> dropTable;
        private AIType aiType = AIType.AGGRESSIVE;
        private Set<String> tags;

        public Builder name(String name) { this.name = name; return this; }
        public Builder health(int health) { this.health = health; return this; }
        public Builder attack(int attack) { this.attack = attack; return this; }
        public Builder defense(int defense) { this.defense = defense; return this; }
        public Builder level(int level) { this.level = level; return this; }
        public Builder exp(int exp) { this.exp = exp; return this; }
        public Builder gold(int gold) { this.gold = gold; return this; }
        public Builder aiType(AIType aiType) { this.aiType = aiType; return this; }

        public Builder addDropItem(DropItem item) {
            if (dropTable == null) dropTable = new ArrayList<>();
            dropTable.add(item);
            return this;
        }

        public Builder addTag(String tag) {
            if (tags == null) tags = new HashSet<>();
            tags.add(tag);
            return this;
        }

        public Monster build() {
            Objects.requireNonNull(name, "怪物名不能为空");
            return new Monster(this);
        }
    }

    public List<Item> rollDrops(Random random) {
        List<Item> drops = new ArrayList<>();
        for (DropItem drop : dropTable) {
            if (random.nextDouble() < drop.getChance()) {
                Item item = drop.asItem();
                if (item != null) drops.add(item);
            }
        }
        return drops;
    }

    public boolean isAlive() { return health > 0; }
    public void takeDamage(int damage) { this.health = Math.max(this.health - damage, 0); }
    public boolean hasTag(String tag) { return tags.contains(tag); }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getLevel() { return level; }
    public int getExperienceReward() { return experienceReward; }
    public int getGoldReward() { return goldReward; }
    public List<DropItem> getDropTable() { return dropTable; }
    public AIType getAiType() { return aiType; }
    public Set<String> getTags() { return tags; }
}
