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
 * Estado RPG persistente de um jogador. Anexado ao jogador via Fabric Data Attachment API
 * (persistente e copiado na morte). É mutável: o codec lê os campos no momento do salvamento.
 */
public final class PlayerRpgData {
    public static final Codec<PlayerRpgData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("class", "none").forGetter(d -> d.rpgClass.id()),
            Codec.STRING.optionalFieldOf("subclass", "none").forGetter(d -> d.subclass.id()),
            Codec.INT.optionalFieldOf("level", 1).forGetter(d -> d.level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(d -> d.xp),
            Codec.FLOAT.optionalFieldOf("mana", 100f).forGetter(d -> d.mana),
            Codec.unboundedMap(Codec.STRING, Codec.LONG).optionalFieldOf("cooldowns", Map.of()).forGetter(d -> d.cooldowns),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("active_quests", Map.of()).forGetter(d -> d.activeQuests),
            Codec.STRING.listOf().optionalFieldOf("completed_quests", List.of()).forGetter(d -> new ArrayList<>(d.completedQuests))
    ).apply(i, PlayerRpgData::new));

    public RpgClass rpgClass;
    public RpgSubclass subclass;
    public int level;
    public int xp;
    public float mana;
    /** chave da habilidade -> tick do servidor em que o cooldown termina. */
    public final Map<String, Long> cooldowns;
    /** id da missão -> progresso atual. */
    public final Map<String, Integer> activeQuests;
    public final Set<String> completedQuests;

    public PlayerRpgData() {
        this("none", "none", 1, 0, 100f, Map.of(), Map.of(), List.of());
    }

    private PlayerRpgData(String cls, String sub, int level, int xp, float mana,
                          Map<String, Long> cooldowns, Map<String, Integer> active, List<String> completed) {
        this.rpgClass = RpgClass.byId(cls);
        this.subclass = RpgSubclass.byId(sub);
        this.level = Math.max(1, level);
        this.xp = Math.max(0, xp);
        this.mana = mana;
        this.cooldowns = new HashMap<>(cooldowns);
        this.activeQuests = new HashMap<>(active);
        this.completedQuests = new HashSet<>(completed);
    }

    public boolean hasClass() {
        return rpgClass != RpgClass.NONE;
    }

    public long cooldownRemaining(String key, long now) {
        return Math.max(0, cooldowns.getOrDefault(key, 0L) - now);
    }
}
