package com.fod.system.factory;

import com.fod.entity.*;

/**
 * 实体工厂 - 统一创建所有游戏实体
 */
public class EntityFactory {

    private EntityFactory() {}

    // ==================== 玩家 ====================
    public static Player createDefaultPlayer(String name) {
        return new Player.Builder()
                .name(name).health(100).attack(10).defense(5).level(1)
                .build();
    }

    // ==================== 怪物 ====================
    public static Monster createGoblin() {
        return new Monster.Builder()
                .name("哥布林").health(30).attack(5).defense(2)
                .level(1).exp(20).gold(10).aiType(Monster.AIType.AGGRESSIVE)
                .addDropItem(new DropItem(Item.createPotion("小型药水", 15, Item.Rarity.COMMON, 5), 0.3))
                .addTag("humanoid")
                .build();
    }

    public static Monster createCaveSpider() {
        return new Monster.Builder()
                .name("洞穴蜘蛛").health(40).attack(8).defense(3)
                .level(2).exp(30).gold(20).aiType(Monster.AIType.GUARD)
                .addDropItem(new DropItem(Item.createPotion("中型药水", 30, Item.Rarity.UNCOMMON, 10), 0.2))
                .addTag("beast")
                .build();
    }

    public static Monster createSkeleton() {
        return new Monster.Builder()
                .name("骷髅战士").health(50).attack(10).defense(4)
                .level(3).exp(40).gold(30).aiType(Monster.AIType.DEFENSIVE)
                .addDropItem(new DropItem(new Equipment.Builder()
                        .name("铁骨剑").description("骷髅掉落的武器")
                        .slot(Equipment.Slot.WEAPON).rarity(Item.Rarity.UNCOMMON)
                        .attackBonus(5).value(30).build(), 0.15))
                .addTag("undead")
                .build();
    }

    public static Monster createBossDragon() {
        return new Monster.Builder()
                .name("远古巨龙").health(200).attack(20).defense(10)
                .level(10).exp(500).gold(300).aiType(Monster.AIType.BOSS)
                .addDropItem(new DropItem(new Equipment.Builder()
                        .name("龙鳞甲").description("由远古巨龙鳞片制成的铠甲")
                        .slot(Equipment.Slot.ARMOR).rarity(Item.Rarity.LEGENDARY)
                        .defenseBonus(20).requiredLevel(8).value(500).build(), 0.5))
                .addTag("boss").addTag("dragon")
                .build();
    }

    // ==================== 物品 ====================
    public static Item createHealthPotion() {
        return Item.createPotion("生命药水", 30, Item.Rarity.COMMON, 10);
    }

    public static Item createLargeHealthPotion() {
        return Item.createPotion("大型生命药水", 60, Item.Rarity.UNCOMMON, 25);
    }

    public static Item createMegaHealthPotion() {
        return Item.createPotion("超级生命药水", 100, Item.Rarity.RARE, 50);
    }

    public static Equipment createIronSword() {
        return new Equipment.Builder()
                .name("铁剑").description("一把普通的铁剑")
                .slot(Equipment.Slot.WEAPON).rarity(Item.Rarity.COMMON)
                .attackBonus(3).value(15).build();
    }

    public static Equipment createLeatherArmor() {
        return new Equipment.Builder()
                .name("皮甲").description("轻便的皮革护甲")
                .slot(Equipment.Slot.ARMOR).rarity(Item.Rarity.COMMON)
                .defenseBonus(5).value(20).build();
    }
}
