package com.europa.rpg.rpg;

import net.minecraft.util.Formatting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Subclasse: liberada no nível {@link Progression#SUBCLASS_LEVEL}; concede um poder supremo e bônus. */
public final class RpgSubclass {
    private static final Map<String, RpgSubclass> ALL = new LinkedHashMap<>();

    public static final RpgSubclass NONE = new RpgSubclass("none", RpgClass.NONE, Formatting.GRAY, "", StatBlock.NONE, false);

    public static final RpgSubclass BERSERKER = reg("berserker", RpgClass.WARRIOR, Formatting.DARK_RED, "blood_rage", StatBlock.builder().damage(2, 0).build());
    public static final RpgSubclass PALADIN = reg("paladin", RpgClass.WARRIOR, Formatting.YELLOW, "divine_shield", StatBlock.builder().health(4, 0).armor(4, 0).build());
    public static final RpgSubclass PYROMANCER = reg("pyromancer", RpgClass.MAGE, Formatting.GOLD, "meteor", StatBlock.builder().mana(50, 0).build());
    public static final RpgSubclass CRYOMANCER = reg("cryomancer", RpgClass.MAGE, Formatting.BLUE, "blizzard", StatBlock.builder().mana(50, 0).armor(2, 0).build());
    public static final RpgSubclass ASSASSIN = reg("assassin", RpgClass.ROGUE, Formatting.GRAY, "death_mark", StatBlock.builder().damage(1.5, 0).build());
    public static final RpgSubclass SHADOWDANCER = reg("shadowdancer", RpgClass.ROGUE, Formatting.DARK_PURPLE, "smoke_bomb", StatBlock.builder().speed(0.01).build());
    public static final RpgSubclass SNIPER = reg("sniper", RpgClass.RANGER, Formatting.DARK_AQUA, "piercing_shot", StatBlock.builder().damage(1, 0).build());
    public static final RpgSubclass BEASTMASTER = reg("beastmaster", RpgClass.RANGER, Formatting.DARK_GREEN, "call_of_the_pack", StatBlock.builder().health(4, 0).build());
    public static final RpgSubclass HIEROPHANT = reg("hierophant", RpgClass.CLERIC, Formatting.WHITE, "sanctuary", StatBlock.builder().mana(40, 0).health(2, 0).build());
    public static final RpgSubclass INQUISITOR = reg("inquisitor", RpgClass.CLERIC, Formatting.GOLD, "judgment", StatBlock.builder().damage(1.5, 0).armor(2, 0).build());
    public static final RpgSubclass LEGION_LORD = reg("legion_lord", RpgClass.NECROMANCER, Formatting.GRAY, "unholy_legion", StatBlock.builder().mana(40, 0).build());
    public static final RpgSubclass HEMOMANCER = reg("hemomancer", RpgClass.NECROMANCER, Formatting.DARK_RED, "blood_pact", StatBlock.builder().health(4, 0).build());
    public static final RpgSubclass SHAPESHIFTER = reg("shapeshifter", RpgClass.DRUID, Formatting.GOLD, "beast_form", StatBlock.builder().health(4, 0).damage(1, 0).build());
    public static final RpgSubclass GROVE_WARDEN = reg("grove_warden", RpgClass.DRUID, Formatting.GREEN, "ancestral_roots", StatBlock.builder().mana(40, 0).armor(2, 0).build());
    public static final RpgSubclass IRON_FIST = reg("iron_fist", RpgClass.MONK, Formatting.GRAY, "hundred_strikes", StatBlock.builder().damage(2, 0).build());
    public static final RpgSubclass WINDWALKER = reg("windwalker", RpgClass.MONK, Formatting.AQUA, "wind_step", StatBlock.builder().speed(0.015).build());
    public static final RpgSubclass MINSTREL = reg("minstrel", RpgClass.BARD, Formatting.YELLOW, "restoring_ballad", StatBlock.builder().mana(40, 0).health(2, 0).build());
    public static final RpgSubclass DISCORDANT = reg("discordant", RpgClass.BARD, Formatting.DARK_PURPLE, "dissonant_chord", StatBlock.builder().damage(1, 0).build());
    public static final RpgSubclass PLAGUEBRINGER = reg("plaguebringer", RpgClass.ALCHEMIST, Formatting.DARK_GREEN, "toxic_cloud", StatBlock.builder().mana(30, 0).build());
    public static final RpgSubclass TRANSMUTER = reg("transmuter", RpgClass.ALCHEMIST, Formatting.GOLD, "perfect_elixir", StatBlock.builder().health(4, 0).build());
    public static final RpgSubclass RUNEFORGER = reg("runeforger", RpgClass.RUNESMITH, Formatting.BLUE, "runic_armor", StatBlock.builder().armor(4, 0).build());
    public static final RpgSubclass ARCANE_ARCHITECT = reg("arcane_architect", RpgClass.RUNESMITH, Formatting.AQUA, "rune_turret", StatBlock.builder().mana(40, 0).build());
    public static final RpgSubclass ASTRAL_WANDERER = reg("astral_wanderer", RpgClass.VOIDWEAVER, Formatting.LIGHT_PURPLE, "stellar_collapse", StatBlock.builder().mana(40, 0).build());
    public static final RpgSubclass VOID_DEVOURER = reg("void_devourer", RpgClass.VOIDWEAVER, Formatting.DARK_AQUA, "devouring_rift", StatBlock.builder().health(4, 0).build());

    public final String id;
    public final RpgClass parent;
    public final Formatting color;
    public final String ultimateAbility;
    public final StatBlock stats;

    private RpgSubclass(String id, RpgClass parent, Formatting color, String ultimate, StatBlock stats, boolean register) {
        this.id = id;
        this.parent = parent;
        this.color = color;
        this.ultimateAbility = ultimate;
        this.stats = stats;
        if (register) {
            ALL.put(id, this);
            parent.subclasses.add(this);
        }
    }

    private static RpgSubclass reg(String id, RpgClass parent, Formatting color, String ultimate, StatBlock stats) {
        return new RpgSubclass(id, parent, color, ultimate, stats, true);
    }

    /** Garante que a classe foi inicializada (popula as listas de subclasses das classes). */
    static void bootstrap() {}

    public String id() { return id; }

    public String translationKey() { return "rpg.europa.subclass." + id; }

    public static RpgSubclass byId(String id) {
        return ALL.getOrDefault(id, NONE);
    }

    public static List<RpgSubclass> all() {
        return List.copyOf(ALL.values());
    }

    @Override
    public String toString() { return id; }
}
