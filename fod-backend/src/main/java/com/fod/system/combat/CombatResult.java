package com.fod.system.combat;

/**
 * 战斗策略执行结果 - 不可变对象
 */
public final class CombatResult {

    private final int damageToMonster;
    private final int damageToPlayer;
    private final boolean isCritical;
    private final boolean isDodged;
    private final String description;
    private final int healAmount;

    public CombatResult(int damageToMonster, int damageToPlayer,
                        boolean isCritical, boolean isDodged,
                        String description, int healAmount) {
        this.damageToMonster = damageToMonster;
        this.damageToPlayer = damageToPlayer;
        this.isCritical = isCritical;
        this.isDodged = isDodged;
        this.description = description;
        this.healAmount = healAmount;
    }

    public int getDamageToMonster() { return damageToMonster; }
    public int getDamageToPlayer() { return damageToPlayer; }
    public boolean isCritical() { return isCritical; }
    public boolean isDodged() { return isDodged; }
    public String getDescription() { return description; }
    public int getHealAmount() { return healAmount; }

    public static class Builder {
        private int damageToMonster;
        private int damageToPlayer;
        private boolean isCritical;
        private boolean isDodged;
        private String description = "";
        private int healAmount;

        public Builder damageToMonster(int dmg) { this.damageToMonster = dmg; return this; }
        public Builder damageToPlayer(int dmg) { this.damageToPlayer = dmg; return this; }
        public Builder isCritical(boolean crit) { this.isCritical = crit; return this; }
        public Builder isDodged(boolean dodge) { this.isDodged = dodge; return this; }
        public Builder description(String desc) { this.description = desc; return this; }
        public Builder healAmount(int heal) { this.healAmount = heal; return this; }

        public CombatResult build() {
            return new CombatResult(damageToMonster, damageToPlayer, isCritical, isDodged, description, healAmount);
        }
    }
}
