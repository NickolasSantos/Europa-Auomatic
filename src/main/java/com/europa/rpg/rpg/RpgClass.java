package com.europa.rpg.rpg;

import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Definição de classe base. Registradas em {@link #ALL}; ids persistidos nos dados do jogador. */
public final class RpgClass {
    private static final Map<String, RpgClass> ALL = new LinkedHashMap<>();

    public static final RpgClass NONE = new RpgClass("none", Formatting.GRAY, "", "", StatBlock.NONE, false);

    public static final RpgClass WARRIOR = reg("warrior", Formatting.RED, "charge", "minecraft:iron_sword",
            StatBlock.builder().health(4, 0.5).armor(2, 0.1).damage(0, 0.05).build());
    public static final RpgClass MAGE = reg("mage", Formatting.AQUA, "arcane_missile", "europa:arcane_staff",
            StatBlock.builder().health(0, 0.25).mana(50, 4).manaRegen(3f).build());
    public static final RpgClass ROGUE = reg("rogue", Formatting.DARK_GREEN, "shadow_step", "minecraft:iron_sword",
            StatBlock.builder().health(0, 0.3).damage(0, 0.04).speed(0.01).attackSpeed(0.3).manaRegen(1.5f).build());
    public static final RpgClass RANGER = reg("ranger", Formatting.GREEN, "volley", "minecraft:bow",
            StatBlock.builder().health(2, 0.35).speed(0.005).manaRegen(1.5f).build());
    public static final RpgClass CLERIC = reg("cleric", Formatting.YELLOW, "restoring_light", "minecraft:golden_apple",
            StatBlock.builder().health(2, 0.4).armor(1, 0.05).mana(30, 2).manaRegen(2.5f).build());
    public static final RpgClass NECROMANCER = reg("necromancer", Formatting.DARK_PURPLE, "raise_servant", "minecraft:wither_skeleton_skull",
            StatBlock.builder().health(0, 0.3).mana(40, 3).manaRegen(2.5f).build());
    public static final RpgClass DRUID = reg("druid", Formatting.DARK_GREEN, "thorn_snare", "minecraft:oak_sapling",
            StatBlock.builder().health(2, 0.4).mana(30, 2).manaRegen(2f).build());
    public static final RpgClass MONK = reg("monk", Formatting.GOLD, "seismic_palm", "minecraft:bamboo",
            StatBlock.builder().health(2, 0.4).armor(1, 0.08).speed(0.01).attackSpeed(0.4).manaRegen(1.5f).build());
    public static final RpgClass BARD = reg("bard", Formatting.LIGHT_PURPLE, "song_of_valor", "minecraft:note_block",
            StatBlock.builder().health(1, 0.3).speed(0.005).mana(30, 2).manaRegen(2.5f).build());
    public static final RpgClass ALCHEMIST = reg("alchemist", Formatting.DARK_AQUA, "volatile_flask", "minecraft:splash_potion",
            StatBlock.builder().health(1, 0.3).mana(30, 2).manaRegen(2f).build());
    public static final RpgClass RUNESMITH = reg("runesmith", Formatting.GRAY, "explosive_rune", "europa:adamantite_ingot",
            StatBlock.builder().health(3, 0.4).armor(2, 0.08).mana(20, 2).manaRegen(2f).build());
    public static final RpgClass VOIDWEAVER = reg("voidweaver", Formatting.BLUE, "void_rift", "minecraft:ender_eye",
            StatBlock.builder().health(0, 0.3).mana(50, 3).manaRegen(2.5f).speed(0.005).build());

    public final String id;
    public final Formatting color;
    public final String primaryAbility;
    /** Item usado como ícone na interface. */
    public final String icon;
    public final StatBlock stats;
    final List<RpgSubclass> subclasses = new ArrayList<>();

    private RpgClass(String id, Formatting color, String primaryAbility, String icon, StatBlock stats, boolean register) {
        this.id = id;
        this.color = color;
        this.primaryAbility = primaryAbility;
        this.icon = icon;
        this.stats = stats;
        if (register) ALL.put(id, this);
    }

    private static RpgClass reg(String id, Formatting color, String ability, String icon, StatBlock stats) {
        return new RpgClass(id, color, ability, icon, stats, true);
    }

    public String id() { return id; }

    public String translationKey() { return "rpg.europa.class." + id; }

    public List<RpgSubclass> subclasses() {
        RpgSubclass.bootstrap();
        return Collections.unmodifiableList(subclasses);
    }

    public static RpgClass byId(String id) {
        return ALL.getOrDefault(id, NONE);
    }

    /** Todas as classes jogáveis (sem NONE), em ordem de registro. */
    public static List<RpgClass> all() {
        return List.copyOf(ALL.values());
    }

    @Override
    public String toString() { return id; }
}
