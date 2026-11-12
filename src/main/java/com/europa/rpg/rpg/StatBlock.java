package com.europa.rpg.rpg;

/**
 * Bônus de atributos. Valores "perLevel" são multiplicados pelo nível do jogador.
 * speed e attackSpeed são somados diretamente ao atributo base (0.1 = velocidade normal de andar).
 */
public record StatBlock(double health, double healthPerLevel, double armor, double armorPerLevel,
                        double damage, double damagePerLevel, double speed, double attackSpeed,
                        float mana, float manaPerLevel, float manaRegen) {
    public static final StatBlock NONE = new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1f);

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private double health, healthPerLevel, armor, armorPerLevel, damage, damagePerLevel, speed, attackSpeed;
        private float mana, manaPerLevel, manaRegen = 1f;

        public Builder health(double base, double perLevel) { health = base; healthPerLevel = perLevel; return this; }
        public Builder armor(double base, double perLevel) { armor = base; armorPerLevel = perLevel; return this; }
        public Builder damage(double base, double perLevel) { damage = base; damagePerLevel = perLevel; return this; }
        public Builder speed(double v) { speed = v; return this; }
        public Builder attackSpeed(double v) { attackSpeed = v; return this; }
        public Builder mana(float base, float perLevel) { mana = base; manaPerLevel = perLevel; return this; }
        public Builder manaRegen(float v) { manaRegen = v; return this; }

        public StatBlock build() {
            return new StatBlock(health, healthPerLevel, armor, armorPerLevel, damage, damagePerLevel, speed, attackSpeed, mana, manaPerLevel, manaRegen);
        }
    }
}
