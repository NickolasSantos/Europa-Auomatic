package com.europa.rpg.economy;

import com.europa.rpg.profession.Profession;
import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.RpgAttachments;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;

/** Facções do documento mestre e reputação (0 Neutro .. 4 Exaltado). */
public final class Factions {
    public record Faction(String id, Formatting color, String icon) {
        public String translationKey() { return "rpg.europa.faction." + id; }
    }

    public static final List<Faction> ALL = List.of(
            new Faction("observatory", Formatting.AQUA, "minecraft:spyglass"),
            new Faction("wanderers", Formatting.GREEN, "minecraft:compass"),
            new Faction("gears", Formatting.GOLD, "minecraft:clock"),
            new Faction("nameless", Formatting.DARK_PURPLE, "minecraft:echo_shard"));
    public static final int[] RANK_THRESHOLDS = {0, 250, 1000, 2500, 5000};
    public static final int MAX_REP = 10000;

    /** Profissões que rendem reputação a cada facção. */
    private static final Map<Profession, String> PROFESSION_FACTION = Map.ofEntries(
            Map.entry(Profession.RUIN_ARCHAEOLOGIST, "observatory"), Map.entry(Profession.DIMENSIONAL_NAVIGATOR, "observatory"),
            Map.entry(Profession.RELIC_CARTOMANCER, "observatory"),
            Map.entry(Profession.ANOMALY_GEOLOGIST, "wanderers"), Map.entry(Profession.BIOME_GARDENER, "wanderers"),
            Map.entry(Profession.PHENOMENON_TAMER, "wanderers"),
            Map.entry(Profession.AUTOMATON_ENGINEER, "gears"), Map.entry(Profession.ARTIFACT_RESTORER, "gears"),
            Map.entry(Profession.METAMORPH_COOK, "gears"),
            Map.entry(Profession.ESSENCE_COLLECTOR, "nameless"), Map.entry(Profession.ECHO_WEAVER, "nameless"),
            Map.entry(Profession.MEMORY_DISTILLER, "nameless"));

    private Factions() {}

    public static Faction byId(String id) {
        return ALL.stream().filter(f -> f.id.equals(id)).findFirst().orElse(null);
    }

    public static int rep(PlayerRpgData d, String faction) {
        return d.reputation.getOrDefault(faction, 0);
    }

    public static int rank(int rep) {
        int r = 0;
        for (int i = 0; i < RANK_THRESHOLDS.length; i++) if (rep >= RANK_THRESHOLDS[i]) r = i;
        return r;
    }

    public static String rankKey(int rank) {
        return "rpg.europa.faction.rank." + rank;
    }

    public static void addRep(ServerPlayerEntity p, String faction, int amount) {
        Faction f = byId(faction);
        if (f == null || amount <= 0) return;
        PlayerRpgData d = RpgAttachments.get(p);
        int before = rep(d, faction);
        int after = Math.min(MAX_REP, before + amount);
        d.reputation.put(faction, after);
        if (rank(after) > rank(before)) {
            p.sendMessage(Text.translatable("rpg.europa.faction.rank_up", Text.translatable(f.translationKey()).formatted(f.color),
                    Text.translatable(rankKey(rank(after)))).formatted(Formatting.GOLD, Formatting.BOLD));
            p.getServerWorld().playSound(null, p.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.7f, 1.3f);
        }
    }

    /** Uma parte do XP de profissão vira reputação (1 a cada 5 de XP, com arredondamento probabilístico). */
    public static void onProfessionXp(ServerPlayerEntity p, Profession prof, int xp) {
        String faction = PROFESSION_FACTION.get(prof);
        if (faction == null) return;
        int rep = xp / 5 + (p.getRandom().nextInt(5) < xp % 5 ? 1 : 0);
        addRep(p, faction, rep);
    }
}
