package com.europa.rpg.rpg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Estado RPG persistente de um jogador (classe, nível, mana, recargas, missões, talentos, patrono, profissões).
 * Anexado via Fabric Data Attachment API; mutável, o codec lê os campos no momento do salvamento.
 * O mesmo codec é usado para sincronizar o estado completo com o cliente.
 */
public final class PlayerRpgData {
    private static final Codec<Map<String, Integer>> INT_MAP = Codec.unboundedMap(Codec.STRING, Codec.INT);

    public static final Codec<PlayerRpgData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("class", "none").forGetter(d -> d.rpgClass.id()),
            Codec.STRING.optionalFieldOf("subclass", "none").forGetter(d -> d.subclass.id()),
            Codec.INT.optionalFieldOf("level", 1).forGetter(d -> d.level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(d -> d.xp),
            Codec.FLOAT.optionalFieldOf("mana", 100f).forGetter(d -> d.mana),
            Codec.unboundedMap(Codec.STRING, Codec.LONG).optionalFieldOf("cooldowns", Map.of()).forGetter(d -> d.cooldowns),
            INT_MAP.optionalFieldOf("active_quests", Map.of()).forGetter(d -> d.activeQuests),
            Codec.STRING.listOf().optionalFieldOf("completed_quests", List.of()).forGetter(d -> new ArrayList<>(d.completedQuests)),
            INT_MAP.optionalFieldOf("talents", Map.of()).forGetter(d -> d.talents),
            Codec.STRING.optionalFieldOf("patron", "").forGetter(d -> d.patron),
            Codec.INT.optionalFieldOf("favor", 0).forGetter(d -> d.favor),
            INT_MAP.optionalFieldOf("prof_levels", Map.of()).forGetter(d -> d.profLevels),
            INT_MAP.optionalFieldOf("prof_xp", Map.of()).forGetter(d -> d.profXp)
    ).apply(i, PlayerRpgData::new));

    public RpgClass rpgClass;
    public RpgSubclass subclass;
    public int level;
    public int xp;
    public float mana;
    /** chave ("primary", "ultimate", "last_stand") -> tick do servidor em que a recarga termina. */
    public final Map<String, Long> cooldowns;
    public final Map<String, Integer> activeQuests;
    public final Set<String> completedQuests;
    /** id do talento -> rank. */
    public final Map<String, Integer> talents;
    /** id do patrono ("" = nenhum). */
    public String patron;
    public int favor;
    public final Map<String, Integer> profLevels;
    public final Map<String, Integer> profXp;

    public PlayerRpgData() {
        this("none", "none", 1, 0, 100f, Map.of(), Map.of(), List.of(), Map.of(), "", 0, Map.of(), Map.of());
    }

    private PlayerRpgData(String cls, String sub, int level, int xp, float mana, Map<String, Long> cooldowns,
                          Map<String, Integer> active, List<String> completed, Map<String, Integer> talents,
                          String patron, int favor, Map<String, Integer> profLevels, Map<String, Integer> profXp) {
        this.rpgClass = RpgClass.byId(cls);
        this.subclass = RpgSubclass.byId(sub);
        this.level = Math.max(1, level);
        this.xp = Math.max(0, xp);
        this.mana = mana;
        this.cooldowns = new HashMap<>(cooldowns);
        this.activeQuests = new HashMap<>(active);
        this.completedQuests = new HashSet<>(completed);
        this.talents = new HashMap<>(talents);
        this.patron = patron;
        this.favor = favor;
        this.profLevels = new HashMap<>(profLevels);
        this.profXp = new HashMap<>(profXp);
    }

    public boolean hasClass() {
        return rpgClass != RpgClass.NONE;
    }

    public long cooldownRemaining(String key, long now) {
        return Math.max(0, cooldowns.getOrDefault(key, 0L) - now);
    }
}
