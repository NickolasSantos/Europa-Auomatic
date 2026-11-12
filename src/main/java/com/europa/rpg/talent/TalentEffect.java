package com.europa.rpg.talent;

/** Tipos de efeito de talento. O valor de cada nó é multiplicado pelo rank. */
public enum TalentEffect {
    ATTACK_DAMAGE,   // + dano corpo a corpo (atributo)
    CRIT_CHANCE,     // chance (0..1) de causar 150% de dano
    ABILITY_POWER,   // + fração de poder das habilidades
    LIFESTEAL,       // fração do dano causado convertida em vida
    EXECUTE,         // + fração de dano contra alvos com < 30% de vida
    MAX_HEALTH,      // + vida máxima (atributo)
    ARMOR,           // + armadura (atributo)
    TOUGHNESS,       // + robustez de armadura e resistência a repulsão
    REGEN,           // vida regenerada a cada 5 s
    LAST_STAND,      // sobrevive a um golpe fatal (recarga 120 s)
    MAX_MANA,        // + mana máxima
    MANA_REGEN,      // + mana por segundo
    COOLDOWN,        // - fração da recarga das habilidades
    MANA_COST,       // - fração do custo de mana
    OVERLOAD         // chance (0..1) de a habilidade não entrar em recarga
}
