package com.europa.rpg.client;

import com.europa.rpg.ability.Abilities;
import com.europa.rpg.ability.AbilityDef;
import com.europa.rpg.ability.AbilityHandler;
import com.europa.rpg.blessing.Patron;
import com.europa.rpg.network.RpgActionPayload;
import com.europa.rpg.profession.Profession;
import com.europa.rpg.profession.Professions;
import com.europa.rpg.rpg.PlayerRpgData;
import com.europa.rpg.rpg.Progression;
import com.europa.rpg.rpg.RpgClass;
import com.europa.rpg.rpg.RpgSubclass;
import com.europa.rpg.talent.TalentNode;
import com.europa.rpg.talent.Talents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Tela de personagem (tecla K): ficha/escolha de classe, talentos, bênção e profissões. */
public class CharacterScreen extends Screen {
    private static final int W = 384, H = 240;
    private static final int TAB_SHEET = 0, TAB_RACE = 1, TAB_TALENTS = 2, TAB_BLESSING = 3, TAB_PROFESSIONS = 4, TAB_FACTIONS = 5;
    private static final String[] TABS = {"sheet", "race", "talents", "blessing", "professions", "factions"};
    private static int lastTab = TAB_SHEET;
    private static RpgClass.Category pickerCategory = RpgClass.Category.MARTIAL;

    private int tab;
    private int left, top;
    private int seenVersion;

    public CharacterScreen(int tab) {
        super(Text.translatable("rpg.europa.screen.title"));
        this.tab = tab < 0 ? lastTab : tab;
    }

    private static PlayerRpgData data() {
        return ClientRpgState.full;
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        seenVersion = ClientRpgState.version;
        int tw = (W - 8) / TABS.length;
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            ButtonWidget b = ButtonWidget.builder(Text.translatable("rpg.europa.screen.tab." + TABS[i]), btn -> {
                tab = idx;
                lastTab = idx;
                clearAndInit();
            }).dimensions(left + 4 + i * tw, top + 4, tw - 2, 18).build();
            b.active = i != tab;
            addDrawableChild(b);
        }
        switch (tab) {
            case TAB_SHEET -> initSheet();
            case TAB_RACE -> initRace();
            case TAB_TALENTS -> initTalents();
            case TAB_BLESSING -> initBlessing();
            default -> {}
        }
    }

    @Override
    public void tick() {
        if (seenVersion != ClientRpgState.version) clearAndInit();
    }

    private static void send(String action, String arg) {
        ClientPlayNetworking.send(new RpgActionPayload(action, arg));
    }

    // ------------------------------------------------------------------ ficha

    private void initSheet() {
        PlayerRpgData d = data();
        if (!d.hasClass()) {
            addDrawableChild(ButtonWidget.builder(Text.translatable("rpg.europa.screen.open_picker").formatted(Formatting.BOLD),
                    b -> client.setScreen(new ClassPickerScreen())).dimensions(left + W / 2 - 80, top + 110, 160, 22).build());
            return;
        }
        if (d.level >= Progression.SUBCLASS_LEVEL && d.subclass == RpgSubclass.NONE) {
            List<RpgSubclass> subs = d.rpgClass.subclasses();
            for (int i = 0; i < subs.size(); i++) {
                RpgSubclass s = subs.get(i);
                addDrawableChild(ButtonWidget.builder(Text.translatable(s.translationKey()).formatted(s.color), b -> send(RpgActionPayload.CHOOSE_SUBCLASS, s.id))
                        .dimensions(left + 10 + i * 122, top + H - 30, 118, 20).tooltip(Tooltip.of(subclassTooltip(s))).build());
            }
        }
    }

    private static Text classTooltip(RpgClass c) {
        MutableText t = Text.translatable(c.translationKey()).formatted(c.color, Formatting.BOLD).append("\n")
                .append(Text.translatable(c.translationKey() + ".desc").formatted(Formatting.GRAY)).append("\n")
                .append(Text.translatable("rpg.europa.screen.resource").formatted(Formatting.DARK_AQUA))
                .append(Text.translatable(c.resource.translationKey()).styled(st -> st.withColor(c.resource.color & 0xFFFFFF))).append("\n\n")
                .append(Text.translatable("rpg.europa.ability_label").formatted(Formatting.YELLOW))
                .append(abilityText(c.primaryAbility)).append("\n\n")
                .append(Text.translatable("rpg.europa.screen.subclasses").formatted(Formatting.LIGHT_PURPLE));
        for (RpgSubclass s : c.subclasses()) {
            t.append("\n • ").append(Text.translatable(s.translationKey()).formatted(s.color))
                    .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable("rpg.europa.ability." + s.ultimateAbility));
        }
        return t;
    }

    private static Text subclassTooltip(RpgSubclass s) {
        return Text.translatable(s.translationKey()).formatted(s.color, Formatting.BOLD).append("\n")
                .append(Text.translatable(s.translationKey() + ".desc").formatted(Formatting.GRAY)).append("\n\n")
                .append(Text.translatable("rpg.europa.ultimate_label").formatted(Formatting.YELLOW))
                .append(abilityText(s.ultimateAbility));
    }

    private static MutableText abilityText(String id) {
        AbilityDef def = Abilities.get(id);
        MutableText t = Text.translatable("rpg.europa.ability." + id).formatted(Formatting.WHITE);
        if (def == null) return t;
        t.append(Text.literal(String.format(Locale.ROOT, "  (%.0fs)", def.cooldown() / 20f)).formatted(Formatting.DARK_AQUA));
        return t.append("\n").append(def.description().get().copy().formatted(Formatting.GRAY));
    }

    private void renderSheet(DrawContext ctx) {
        PlayerRpgData d = data();
        int x = left + 10, y = top + 30;
        if (!d.hasClass()) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("rpg.europa.screen.no_class_yet").formatted(Formatting.GOLD), left + W / 2, top + 80, 0xFFFFFF);
            return;
        }
        ctx.fill(x - 1, y - 5, x + 21, y + 17, 0xFFB08D4A);
        Portraits.draw(ctx, d.rpgClass, x, y - 4, 20);
        MutableText title = Text.translatable(d.rpgClass.translationKey()).formatted(d.rpgClass.color, Formatting.BOLD);
        if (d.subclass != RpgSubclass.NONE) title.append(Text.literal(" · ").formatted(Formatting.DARK_GRAY)).append(Text.translatable(d.subclass.translationKey()).formatted(d.subclass.color));
        ctx.drawTextWithShadow(textRenderer, title, x + 20, y + 4, 0xFFFFFF);
        y += 22;
        int needed = d.level >= Progression.MAX_LEVEL ? 0 : Progression.xpForNextLevel(d.level);
        ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.stats.level", d.level, d.xp, needed), x, y, 0xFFFFFF);
        bar(ctx, x + 160, y + 2, 170, 5, needed > 0 ? d.xp / (float) needed : 1f, 0xFF7CFC00);
        y += 12;
        ctx.drawTextWithShadow(textRenderer, Text.translatable(d.rpgClass.resource.translationKey()).append(": " + (int) d.mana + "/" + (int) Progression.maxMana(d)), x, y, d.rpgClass.resource.color);
        com.europa.rpg.race.Race race = com.europa.rpg.race.Races.of(d);
        if (race != null) ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.screen.race_label").append(Text.translatable(race.translationKey()).formatted(race.color())), x + 170, y, 0xFFCCCCCC);
        y += 12;
        if (client != null && client.player != null) {
            var pl = client.player;
            ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.screen.attributes",
                    (int) pl.getMaxHealth(), fmt(pl.getAttributeValue(EntityAttributes.GENERIC_ARMOR)),
                    fmt(pl.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)), fmt(Progression.powerScale(d) * 100) + "%"), x, y, 0xFFCCCCCC);
        }
        y += 12;
        ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.stats.talents", Talents.availablePoints(d)), x, y, 0xFFFFD27F);
        y += 16;
        y = drawAbility(ctx, x, y, "R", AbilityHandler.abilityFor(d, AbilityHandler.PRIMARY), d);
        y = drawAbility(ctx, x, y + 4, "G", AbilityHandler.abilityFor(d, AbilityHandler.ULTIMATE), d);
        y = drawAbility(ctx, x, y + 4, "V", AbilityHandler.abilityFor(d, AbilityHandler.RACIAL), d);
        if (d.level >= Progression.SUBCLASS_LEVEL && d.subclass == RpgSubclass.NONE) {
            ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.choose_subclass").formatted(Formatting.LIGHT_PURPLE), x, top + H - 42, 0xFFFFFF);
        } else if (d.subclass == RpgSubclass.NONE) {
            ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.no_subclass", Progression.SUBCLASS_LEVEL).formatted(Formatting.DARK_GRAY), x, top + H - 20, 0xFFFFFF);
        }
    }

    private int drawAbility(DrawContext ctx, int x, int y, String key, AbilityDef def, PlayerRpgData d) {
        if (def == null) return y;
        MutableText head = Text.literal("[" + key + "] ").formatted(Formatting.YELLOW)
                .append(Text.translatable(def.translationKey()).formatted(Formatting.WHITE));
        int cost = AbilityHandler.effectiveCost(d, def);
        head.append(Text.literal("  ").append(cost > 0 ? Text.literal(cost + " ").append(Text.translatable(d.rpgClass.resource.translationKey())).append(" · ") : Text.empty())
                .append(String.format(Locale.ROOT, "%.0fs", AbilityHandler.effectiveCooldown(d, def) / 20f)).formatted(Formatting.DARK_AQUA));
        ctx.drawTextWithShadow(textRenderer, head, x, y, 0xFFFFFF);
        y += 10;
        for (OrderedText line : textRenderer.wrapLines(def.description().get().copy().formatted(Formatting.GRAY), W - 30)) {
            ctx.drawTextWithShadow(textRenderer, line, x + 8, y, 0xFFFFFF);
            y += 9;
        }
        return y;
    }

    // ------------------------------------------------------------------ talentos

    private void initTalents() {
        PlayerRpgData d = data();
        TalentNode.Branch[] branches = TalentNode.Branch.values();
        int colW = 118;
        for (int b = 0; b < branches.length; b++) {
            for (int tier = 1; tier <= 5; tier++) {
                TalentNode n = Talents.at(branches[b], tier);
                if (n == null) continue;
                int rank = Talents.rank(d, n);
                Talents.Status st = Talents.canUnlock(d, n);
                Formatting color = rank >= n.maxRank() ? Formatting.GREEN : st == Talents.Status.OK ? Formatting.YELLOW : rank > 0 ? Formatting.WHITE : Formatting.GRAY;
                Text label = Text.translatable(n.translationKey()).append(" " + rank + "/" + n.maxRank()).formatted(color);
                ButtonWidget btn = ButtonWidget.builder(label, x -> send(RpgActionPayload.UNLOCK_TALENT, n.id()))
                        .dimensions(left + 10 + b * (colW + 2), top + 56 + (tier - 1) * 34, colW, 20)
                        .tooltip(Tooltip.of(talentTooltip(d, n, st))).build();
                btn.active = st == Talents.Status.OK;
                addDrawableChild(btn);
            }
        }
    }

    private static Text talentTooltip(PlayerRpgData d, TalentNode n, Talents.Status st) {
        int rank = Talents.rank(d, n);
        String per = formatEffect(n, 1), now = formatEffect(n, rank);
        MutableText t = Text.translatable(n.translationKey()).formatted(Formatting.GOLD, Formatting.BOLD).append("\n")
                .append(Text.translatable(n.translationKey() + ".desc", per).formatted(Formatting.GRAY)).append("\n")
                .append(Text.translatable("rpg.europa.talent.rank", rank, n.maxRank(), now).formatted(Formatting.WHITE));
        if (st != Talents.Status.OK && st != Talents.Status.MAXED) {
            t.append("\n").append(Text.translatable("rpg.europa.talent.status." + st.name().toLowerCase(Locale.ROOT), Talents.CAPSTONE_REQUIREMENT).formatted(Formatting.RED));
        }
        return t;
    }

    private static String formatEffect(TalentNode n, int rank) {
        double v = n.valuePerRank() * rank;
        return switch (n.effect()) {
            case CRIT_CHANCE, ABILITY_POWER, LIFESTEAL, EXECUTE, COOLDOWN, MANA_COST, OVERLOAD -> fmt(v * 100) + "%";
            default -> fmt(v);
        };
    }

    private void renderTalents(DrawContext ctx) {
        PlayerRpgData d = data();
        ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.stats.talents", Talents.availablePoints(d)).formatted(Formatting.YELLOW), left + 10, top + 28, 0xFFFFFF);
        TalentNode.Branch[] branches = TalentNode.Branch.values();
        int colW = 118;
        for (int b = 0; b < branches.length; b++) {
            int cx = left + 10 + b * (colW + 2);
            Text head = Text.translatable("rpg.europa.talent.branch." + branches[b].name().toLowerCase(Locale.ROOT))
                    .append(" (" + Talents.pointsInBranch(d, branches[b]) + ")");
            ctx.drawCenteredTextWithShadow(textRenderer, head, cx + colW / 2, top + 42, 0xFFFFD27F);
            for (int tier = 1; tier < 5; tier++) {
                int y1 = top + 56 + (tier - 1) * 34 + 20, y2 = y1 + 14;
                ctx.fill(cx + colW / 2 - 1, y1, cx + colW / 2 + 1, y2, 0xFF6B5B3A);
            }
        }
        if (!d.hasClass()) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("rpg.europa.no_class").formatted(Formatting.RED), left + W / 2, top + H - 14, 0xFFFFFF);
        }
    }

    // ------------------------------------------------------------------ bênção

    private void initBlessing() {
        PlayerRpgData d = data();
        List<Patron> patrons = Patron.all();
        for (int i = 0; i < patrons.size(); i++) {
            Patron p = patrons.get(i);
            boolean current = p.id().equals(d.patron);
            Text label = Text.literal(current ? "★ " : "").append(Text.translatable(p.translationKey())).formatted(p.color());
            ButtonWidget btn = ButtonWidget.builder(label, b -> send(RpgActionPayload.PLEDGE_PATRON, p.id()))
                    .dimensions(left + 10 + (i % 3) * 114, top + 50 + (i / 3) * 24, 110, 20)
                    .tooltip(Tooltip.of(patronTooltip(p))).build();
            btn.active = !current;
            addDrawableChild(btn);
        }
    }

    private static Text patronTooltip(Patron p) {
        MutableText t = Text.translatable(p.translationKey()).formatted(p.color(), Formatting.BOLD).append("\n")
                .append(Text.translatable(p.translationKey() + ".title").formatted(Formatting.ITALIC, Formatting.GRAY)).append("\n");
        for (int tier = 1; tier <= 3; tier++) {
            t.append("\n").append(Text.translatable("rpg.europa.patron.tier", tier).formatted(Formatting.YELLOW))
                    .append(Text.translatable(p.translationKey() + ".tier" + tier).formatted(Formatting.WHITE));
        }
        t.append("\n\n").append(Text.translatable("rpg.europa.patron.offerings").formatted(Formatting.AQUA));
        for (Map.Entry<Identifier, Integer> e : p.offerings().entrySet()) {
            t.append("\n • ").append(Registries.ITEM.get(e.getKey()).getName()).append(Text.literal(" +" + e.getValue()).formatted(Formatting.GREEN));
        }
        return t;
    }

    private void renderBlessing(DrawContext ctx) {
        PlayerRpgData d = data();
        int x = left + 10;
        int hy = top + 29;
        for (OrderedText l : textRenderer.wrapLines(Text.translatable("rpg.europa.patron.pick_hint").formatted(Formatting.GRAY), W - 20)) {
            ctx.drawTextWithShadow(textRenderer, l, x, hy, 0xFFFFFF);
            hy += 9;
        }
        Patron p = d.patron.isEmpty() ? null : Patron.byId(d.patron);
        int y = top + 124;
        if (p == null) {
            ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.patron.none").formatted(Formatting.DARK_GRAY), x, y, 0xFFFFFF);
            return;
        }
        ctx.drawItem(new ItemStack(Registries.ITEM.get(p.icon())), x, y);
        ctx.drawTextWithShadow(textRenderer, Text.translatable(p.translationKey()).formatted(p.color(), Formatting.BOLD)
                .append(Text.literal(" — ").formatted(Formatting.DARK_GRAY))
                .append(Text.translatable(p.translationKey() + ".title").formatted(Formatting.GRAY)), x + 20, y + 4, 0xFFFFFF);
        y += 22;
        int tier = Patron.tier(d.favor);
        ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.patron.favor", d.favor, Patron.MAX_FAVOR), x, y, 0xFFFFFF);
        bar(ctx, x + 130, y + 2, 200, 5, d.favor / (float) Patron.MAX_FAVOR, 0xFFFFC94D);
        int m2 = x + 130 + 200 * Patron.TIER2_FAVOR / Patron.MAX_FAVOR, m3 = x + 130 + 200 * Patron.TIER3_FAVOR / Patron.MAX_FAVOR;
        ctx.fill(m2, y, m2 + 1, y + 9, 0xFFFFFFFF);
        ctx.fill(m3, y, m3 + 1, y + 9, 0xFFFFFFFF);
        y += 14;
        for (int t = 1; t <= 3; t++) {
            boolean on = tier >= t;
            Text line = Text.translatable("rpg.europa.patron.tier", t).formatted(on ? Formatting.YELLOW : Formatting.DARK_GRAY)
                    .append(Text.translatable(p.translationKey() + ".tier" + t).formatted(on ? Formatting.WHITE : Formatting.DARK_GRAY));
            for (OrderedText l : textRenderer.wrapLines(line, W - 24)) {
                ctx.drawTextWithShadow(textRenderer, l, x, y, 0xFFFFFF);
                y += 10;
            }
        }
        ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.patron.offer_hint").formatted(Formatting.GRAY), x, top + H - 14, 0xFFFFFF);
    }

    // ------------------------------------------------------------------ profissões

    private int hoverX, hoverY;
    private List<OrderedText> perkTooltip;

    private void renderProfessions(DrawContext ctx) {
        perkTooltip = null;
        PlayerRpgData d = data();
        int x = left + 10;
        for (Profession p : Profession.values()) {
            int lvl = Professions.level(d, p), xp = Professions.xp(d, p);
            int need = lvl >= Profession.MAX_LEVEL ? 0 : Profession.xpForNext(lvl);
            int col = p.ordinal() % 2, row = p.ordinal() / 2;
            int px = x + col * 184, py = top + 30 + row * 34;
            ctx.drawItem(new ItemStack(Registries.ITEM.get(p.icon)), px, py + 4);
            ctx.drawTextWithShadow(textRenderer, Text.translatable(p.translationKey()).formatted(Formatting.WHITE)
                    .append(Text.literal(" " + lvl).formatted(Formatting.AQUA)), px + 20, py, 0xFFFFFF);
            ctx.drawTextWithShadow(textRenderer, Text.translatable(Profession.tierKey(lvl)).formatted(tierColor(lvl)), px + 20, py + 10, 0xFFFFFF);
            bar(ctx, px + 92, py + 12, 74, 4, need > 0 ? xp / (float) need : 1f, 0xFF4DD2FF);
            String chance = p == Profession.PHENOMENON_TAMER ? "+" + fmt(Professions.fishingLuck(d))
                    : p == Profession.DIMENSIONAL_NAVIGATOR ? "+" + fmt(Professions.travelSpeed(d) * 1000) + "%"
                    : fmt(Professions.perkChance(d, p) * 100) + "%";
            Text perk = Text.translatable(p.translationKey() + ".perk", chance).formatted(Formatting.GRAY);
            int maxW = 160;
            if (textRenderer.getWidth(perk) > maxW) {
                ctx.drawTextWithShadow(textRenderer, net.minecraft.text.StringVisitable.concat(textRenderer.trimToWidth(perk, maxW - 8), Text.literal("…")).getString(), px + 20, py + 20, 0xFFAAAAAA);
                if (hoverX >= px && hoverX < px + 180 && hoverY >= py && hoverY < py + 32) {
                    perkTooltip = textRenderer.wrapLines(perk.copy().formatted(Formatting.WHITE), 200);
                }
            } else {
                ctx.drawTextWithShadow(textRenderer, perk, px + 20, py + 20, 0xFFFFFF);
            }
        }
    }

    private static Formatting tierColor(int lvl) {
        return switch (Profession.tier(lvl)) {
            case 0 -> Formatting.GRAY;
            case 1 -> Formatting.GREEN;
            case 2 -> Formatting.BLUE;
            case 3 -> Formatting.DARK_PURPLE;
            default -> Formatting.GOLD;
        };
    }

    // ------------------------------------------------------------------ raça

    private void initRace() {
        PlayerRpgData d = data();
        List<com.europa.rpg.race.Race> races = com.europa.rpg.race.Race.all();
        int bw = 118, bh = 18;
        for (int i = 0; i < races.size(); i++) {
            com.europa.rpg.race.Race r = races.get(i);
            boolean current = r.id().equals(d.race);
            Text label = Text.literal(current ? "★ " : "").append(Text.translatable(r.translationKey())).formatted(r.color());
            ButtonWidget b = ButtonWidget.builder(label, x -> send(RpgActionPayload.CHOOSE_RACE, r.id()))
                    .dimensions(left + 10 + (i % 3) * (bw + 3), top + 44 + (i / 3) * (bh + 2), bw, bh)
                    .tooltip(Tooltip.of(raceTooltip(r))).build();
            b.active = !d.hasRace();
            addDrawableChild(b);
        }
    }

    private static Text raceTooltip(com.europa.rpg.race.Race r) {
        MutableText t = Text.translatable(r.translationKey()).formatted(r.color(), Formatting.BOLD).append("\n")
                .append(Text.translatable(r.translationKey() + ".desc").formatted(Formatting.GRAY, Formatting.ITALIC)).append("\n");
        com.europa.rpg.race.Race.Traits tr = r.traits();
        if (r.scale() != 1.0) t.append("\n").append(Text.translatable("rpg.europa.trait.scale", fmt(r.scale() * 100) + "%").formatted(Formatting.WHITE));
        trait(t, "health", tr.health(), false);
        trait(t, "armor", tr.armor(), false);
        trait(t, "toughness", tr.toughness(), false);
        trait(t, "damage", tr.damage(), false);
        trait(t, "speed", tr.speed() * 1000, true);
        trait(t, "luck", tr.luck(), false);
        trait(t, "mana", tr.mana(), false);
        trait(t, "mana_regen", tr.manaRegen(), false);
        trait(t, "crit", tr.crit() * 100, true);
        trait(t, "power", tr.power() * 100, true);
        trait(t, "cooldown", tr.cooldown() * 100, true);
        trait(t, "xp", tr.xp() * 100, true);
        t.append("\n").append(Text.translatable("rpg.europa.screen.passive").formatted(Formatting.YELLOW))
                .append(Text.translatable(r.translationKey() + ".passive").formatted(Formatting.WHITE));
        t.append("\n\n").append(Text.translatable("rpg.europa.racial_label").formatted(Formatting.YELLOW)).append(abilityText(r.ability()));
        return t;
    }

    private static void trait(MutableText t, String key, double v, boolean percent) {
        if (v == 0) return;
        String n = (v > 0 ? "+" : "") + fmt(v) + (percent ? "%" : "");
        t.append("\n").append(Text.translatable("rpg.europa.trait." + key, n).formatted(v > 0 ? Formatting.GREEN : Formatting.RED));
    }

    private void renderRace(DrawContext ctx) {
        PlayerRpgData d = data();
        int x = left + 10;
        Text head = d.hasRace() ? Text.translatable("rpg.europa.race_locked") : Text.translatable("rpg.europa.choose_race");
        ctx.drawTextWithShadow(textRenderer, head.copy().formatted(d.hasRace() ? Formatting.GRAY : Formatting.GOLD), x, top + 30, 0xFFFFFF);
        com.europa.rpg.race.Race r = com.europa.rpg.race.Races.of(d);
        if (r == null) {
            ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.screen.hover_race").formatted(Formatting.DARK_GRAY), x, top + H - 16, 0xFFFFFF);
            return;
        }
        int y = top + 172;
        ctx.drawTextWithShadow(textRenderer, Text.translatable(r.translationKey()).formatted(r.color(), Formatting.BOLD)
                .append(Text.literal(" — ").formatted(Formatting.DARK_GRAY))
                .append(Text.translatable(r.translationKey() + ".passive").formatted(Formatting.WHITE)), x, y, 0xFFFFFF);
        AbilityDef def = Abilities.get(r.ability());
        if (def != null) drawAbility(ctx, x, y + 14, "V", def, d);
    }

    // ------------------------------------------------------------------ facções

    private void renderFactions(DrawContext ctx) {
        PlayerRpgData d = data();
        int x = left + 10, y = top + 30;
        for (com.europa.rpg.economy.Factions.Faction f : com.europa.rpg.economy.Factions.ALL) {
            int rep = com.europa.rpg.economy.Factions.rep(d, f.id());
            int rank = com.europa.rpg.economy.Factions.rank(rep);
            int[] th = com.europa.rpg.economy.Factions.RANK_THRESHOLDS;
            int next = rank + 1 < th.length ? th[rank + 1] : com.europa.rpg.economy.Factions.MAX_REP;
            float frac = rank + 1 < th.length ? (rep - th[rank]) / (float) (next - th[rank]) : 1f;
            ctx.drawItem(new ItemStack(Registries.ITEM.get(Identifier.of(f.icon()))), x, y + 4);
            ctx.drawTextWithShadow(textRenderer, Text.translatable(f.translationKey()).formatted(f.color(), Formatting.BOLD)
                    .append(Text.literal("  ").append(Text.translatable(com.europa.rpg.economy.Factions.rankKey(rank))).formatted(Formatting.YELLOW)), x + 20, y, 0xFFFFFF);
            bar(ctx, x + 250, y + 2, 110, 5, frac, 0xFF4DD2A0);
            ctx.drawTextWithShadow(textRenderer, Text.literal(rep + "/" + next).formatted(Formatting.GRAY), x + 250, y + 10, 0xFFFFFF);
            int ly = y + 11;
            for (OrderedText l : textRenderer.wrapLines(Text.translatable(f.translationKey() + ".desc").formatted(Formatting.GRAY), 220)) {
                ctx.drawTextWithShadow(textRenderer, l, x + 20, ly, 0xFFFFFF);
                ly += 9;
            }
            y += 46;
        }
        ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("rpg.europa.faction.hint").formatted(Formatting.DARK_GRAY), left + W / 2, top + H - 12, 0xFFFFFF);
    }

    // ------------------------------------------------------------------ comum

    private static String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.ROOT, "%.1f", v);
    }

    private static void bar(DrawContext ctx, int x, int y, int w, int h, float frac, int color) {
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        ctx.fill(x, y, x + w, y + h, 0xFF2A2A2A);
        ctx.fill(x, y, x + Math.round(w * Math.max(0, Math.min(1, frac))), y + h, color);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(left - 2, top - 2, left + W + 2, top + H + 2, 0xFF8B6F3A);
        ctx.fill(left, top, left + W, top + H, 0xF0181410);
        ctx.fill(left, top + 24, left + W, top + 25, 0xFF8B6F3A);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        switch (tab) {
            case TAB_SHEET -> renderSheet(ctx);
            case TAB_RACE -> renderRace(ctx);
            case TAB_FACTIONS -> renderFactions(ctx);
            case TAB_TALENTS -> renderTalents(ctx);
            case TAB_BLESSING -> renderBlessing(ctx);
            case TAB_PROFESSIONS -> {
                hoverX = mouseX;
                hoverY = mouseY;
                renderProfessions(ctx);
                if (perkTooltip != null) ctx.drawOrderedTooltip(textRenderer, perkTooltip, mouseX, mouseY);
            }
            default -> {}
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
