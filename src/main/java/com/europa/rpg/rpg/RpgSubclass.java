package com.europa.rpg.rpg;

import net.minecraft.util.Formatting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Subclasse: liberada no nível {@link Progression#SUBCLASS_LEVEL}; concede um poder supremo e bônus. */
public final class RpgSubclass {
    private static final Map<String, RpgSubclass> ALL = new LinkedHashMap<>();

    public static final RpgSubclass NONE = new RpgSubclass("none", RpgClass.NONE, Formatting.GRAY, "", StatBlock.NONE, false);

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

    public static RpgSubclass register(String id, RpgClass parent, Formatting color, String ultimate, StatBlock stats) {
        if (ALL.containsKey(id)) throw new IllegalStateException("Subclasse duplicada: " + id);
        return new RpgSubclass(id, parent, color, ultimate, stats, true);
    }

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
