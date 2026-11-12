package com.europa.rpg.race;

import net.minecraft.util.Formatting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Raça: escala corporal (aparência), bônus e limitações de atributos, uma passiva (em {@link Races})
 * e uma habilidade racial ativa (tecla V). Nenhuma raça é exigida por classe alguma.
 */
public record Race(String id, Formatting color, double scale, Traits traits, String ability) {
    /** Modificadores fixos. Valores negativos são as limitações. */
    public record Traits(double health, double armor, double toughness, double damage, double speed, double luck,
                         float mana, float manaRegen, double crit, double power, double cooldown, double xp) {
        public static Builder b() { return new Builder(); }

        public static final class Builder {
            private double health, armor, toughness, damage, speed, luck, crit, power, cooldown, xp;
            private float mana, manaRegen;
            public Builder health(double v) { health = v; return this; }
            public Builder armor(double v) { armor = v; return this; }
            public Builder toughness(double v) { toughness = v; return this; }
            public Builder damage(double v) { damage = v; return this; }
            public Builder speed(double v) { speed = v; return this; }
            public Builder luck(double v) { luck = v; return this; }
            public Builder mana(float v) { mana = v; return this; }
            public Builder manaRegen(float v) { manaRegen = v; return this; }
            public Builder crit(double v) { crit = v; return this; }
            public Builder power(double v) { power = v; return this; }
            public Builder cooldown(double v) { cooldown = v; return this; }
            public Builder xp(double v) { xp = v; return this; }
            public Traits build() { return new Traits(health, armor, toughness, damage, speed, luck, mana, manaRegen, crit, power, cooldown, xp); }
        }
    }

    private static final Map<String, Race> ALL = new LinkedHashMap<>();

    public static Race register(String id, Formatting color, double scale, Traits traits, String ability) {
        Race r = new Race(id, color, scale, traits, ability);
        if (ALL.put(id, r) != null) throw new IllegalStateException("Raça duplicada: " + id);
        return r;
    }

    public static Race byId(String id) {
        return ALL.get(id);
    }

    public static List<Race> all() {
        return List.copyOf(ALL.values());
    }

    public String translationKey() {
        return "rpg.europa.race." + id;
    }
}
