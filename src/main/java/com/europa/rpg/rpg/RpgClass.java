package com.europa.rpg.rpg;

import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Definição de classe base. Registradas pelo {@link com.europa.rpg.content.ClassCatalog}. */
public final class RpgClass {
    public enum Category {
        MARTIAL, DEXTERITY, ARCANE, SPIRITUAL, HYBRID;

        public String translationKey() {
            return "rpg.europa.category." + name().toLowerCase(Locale.ROOT);
        }
    }

    private static final Map<String, RpgClass> ALL = new LinkedHashMap<>();

    public static final RpgClass NONE = new RpgClass("none", Category.MARTIAL, Formatting.GRAY, Resource.MANA, "", "minecraft:barrier", StatBlock.NONE, false);

    public final String id;
    public final Category category;
    public final Formatting color;
    public final Resource resource;
    public final String primaryAbility;
    /** Item usado como ícone na interface. */
    public final String icon;
    public final StatBlock stats;
    final List<RpgSubclass> subclasses = new ArrayList<>();

    private RpgClass(String id, Category category, Formatting color, Resource resource, String primaryAbility, String icon,
                     StatBlock stats, boolean register) {
        this.id = id;
        this.category = category;
        this.color = color;
        this.resource = resource;
        this.primaryAbility = primaryAbility;
        this.icon = icon;
        this.stats = stats;
        if (register) ALL.put(id, this);
    }

    public static RpgClass register(String id, Category category, Formatting color, Resource resource, String ability,
                                    String icon, StatBlock stats) {
        if (ALL.containsKey(id)) throw new IllegalStateException("Classe duplicada: " + id);
        return new RpgClass(id, category, color, resource, ability, icon, stats, true);
    }

    public String id() { return id; }

    public String translationKey() { return "rpg.europa.class." + id; }

    public List<RpgSubclass> subclasses() {
        return Collections.unmodifiableList(subclasses);
    }

    public static RpgClass byId(String id) {
        return ALL.getOrDefault(id, NONE);
    }

    public static List<RpgClass> all() {
        return List.copyOf(ALL.values());
    }

    public static List<RpgClass> byCategory(Category c) {
        return ALL.values().stream().filter(x -> x.category == c).toList();
    }

    @Override
    public String toString() { return id; }
}
