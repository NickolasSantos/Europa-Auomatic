package com.europa.rpg.talent;

/**
 * Nó da árvore. Tier 1..5 dentro de um ramo; para comprar um nó de tier N é preciso ter ao menos
 * 1 ponto no nó de tier N-1 do mesmo ramo. Tier 5 (capstone) exige {@link Talents#CAPSTONE_REQUIREMENT} pontos no ramo.
 */
public record TalentNode(String id, Branch branch, int tier, int maxRank, TalentEffect effect, double valuePerRank) {
    public enum Branch { COMBAT, DEFENSE, ARCANE }

    public String translationKey() {
        return "rpg.europa.talent." + id;
    }
}
