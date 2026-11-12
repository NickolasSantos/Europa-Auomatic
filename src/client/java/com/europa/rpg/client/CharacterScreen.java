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
    private static final int W = 340, H = 236;
    private static final int TAB_SHEET = 0, TAB_TALENTS = 1, TAB_BLESSING = 2, TAB_PROFESSIONS = 3;
    private static int lastTab = TAB_SHEET;

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
        String[] tabs = {"sheet", "talents", "blessing", "professions"};
        int tw = (W - 8) / 4;
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            ButtonWidget b = ButtonWidget.builder(Text.translatable("rpg.europa.screen.tab." + tabs[i]), btn -> {
                tab = idx;
                lastTab = idx;
                clearAndInit();
            }).dimensions(left + 4 + i * tw, top + 4, tw - 2, 18).build();
            b.active = i != tab;
            addDrawableChild(b);
        }
        switch (tab) {
            case TAB_SHEET -> initSheet();
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
            List<RpgClass> classes = RpgClass.all();
            int cols = 4, bw = 80, bh = 20;
            for (int i = 0; i < classes.size(); i++) {
                RpgClass c = classes.get(i);
                int x = left + 10 + (i % cols) * (bw + 2), y = top + 52 + (i / cols) * (bh + 6);
                addDrawableChild(ButtonWidget.builder(Text.translatable(c.translationKey()).formatted(c.color), b -> send(RpgActionPayload.CHOOSE_CLASS, c.id))
                        .dimensions(x, y, bw, bh).tooltip(Tooltip.of(classTooltip(c))).build());
            }
            return;
        }
        if (d.level >= Progression.SUBCLASS_LEVEL && d.subclass == RpgSubclass.NONE) {
            List<RpgSubclass> subs = d.rpgClass.subclasses();
            for (int i = 0; i < subs.size(); i++) {
                RpgSubclass s = subs.get(i);
                addDrawableChild(ButtonWidget.builder(Text.translatable(s.translationKey()).formatted(s.color), b -> send(RpgActionPayload.CHOOSE_SUBCLASS, s.id))
                        .dimensions(left + 10 + i * 162, top + H - 30, 158, 20).tooltip(Tooltip.of(subclassTooltip(s))).build());
            }
        }
    }

    private static Text classTooltip(RpgClass c) {
        MutableText t = Text.translatable(c.translationKey()).formatted(c.color, Formatting.BOLD).append("\n")
                .append(Text.translatable(c.translationKey() + ".desc").formatted(Formatting.GRAY)).append("\n\n")
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
        if (def != null) {
            t.append(Text.literal(String.format(Locale.ROOT, "  (%d mana, %.0fs)", def.manaCost(), def.cooldown() / 20f)).formatted(Formatting.DARK_AQUA));
        }
        return t.append("\n").append(Text.translatable("rpg.europa.ability." + id + ".desc").formatted(Formatting.GRAY));
    }

    private void renderSheet(DrawContext ctx) {
        PlayerRpgData d = data();
        int x = left + 10, y = top + 30;
        if (!d.hasClass()) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("rpg.europa.choose_class").formatted(Formatting.GOLD), left + W / 2, y + 4, 0xFFFFFF);
            ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("rpg.europa.screen.hover_hint").formatted(Formatting.GRAY), left + W / 2, top + 140, 0xFFFFFF);
            return;
        }
        ctx.drawItem(new ItemStack(Registries.ITEM.get(Identifier.of(d.rpgClass.icon))), x, y);
        MutableText title = Text.translatable(d.rpgClass.translationKey()).formatted(d.rpgClass.color, Formatting.BOLD);
        if (d.subclass != RpgSubclass.NONE) title.append(Text.literal(" · ").formatted(Formatting.DARK_GRAY)).append(Text.translatable(d.subclass.translationKey()).formatted(d.subclass.color));
        ctx.drawTextWithShadow(textRenderer, title, x + 20, y + 4, 0xFFFFFF);
        y += 22;
        int needed = d.level >= Progression.MAX_LEVEL ? 0 : Progression.xpForNextLevel(d.level);
        ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.stats.level", d.level, d.xp, needed), x, y, 0xFFFFFF);
        bar(ctx, x + 150, y + 2, 160, 5, needed > 0 ? d.xp / (float) needed : 1f, 0xFF7CFC00);
        y += 12;
        ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.stats.mana", (int) d.mana, (int) Progression.maxMana(d)), x, y, 0xFF9EC5FF);
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
        if (d.level >= Progression.SUBCLASS_LEVEL && d.subclass == RpgSubclass.NONE) {
            ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.choose_subclass").formatted(Formatting.LIGHT_PURPLE), x, top + H - 42, 0xFFFFFF);
        } else if (d.subclass == RpgSubclass.NONE) {
            ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.no_subclass", Progression.SUBCLASS_LEVEL).formatted(Formatting.DARK_GRAY), x, top + H - 20, 0xFFFFFF);
        }
    }

    private int drawAbility(DrawContext ctx, int x, int y, String key, AbilityDef def, PlayerRpgData d) {
        if (def == null) return y;
        Text head = Text.literal("[" + key + "] ").formatted(Formatting.YELLOW)
                .append(Text.translatable(def.translationKey()).formatted(Formatting.WHITE))
                .append(Text.literal(String.format(Locale.ROOT, "  %d mana · %.1fs",
                        AbilityHandler.effectiveCost(d, def), AbilityHandler.effectiveCooldown(d, def) / 20f)).formatted(Formatting.DARK_AQUA));
        ctx.drawTextWithShadow(textRenderer, head, x, y, 0xFFFFFF);
        y += 10;
        for (OrderedText line : textRenderer.wrapLines(Text.translatable(def.translationKey() + ".desc").formatted(Formatting.GRAY), W - 30)) {
            ctx.drawTextWithShadow(textRenderer, line, x + 8, y, 0xFFFFFF);
            y += 9;
        }
        return y;
    }

    // ------------------------------------------------------------------ talentos

    private void initTalents() {
        PlayerRpgData d = data();
        TalentNode.Branch[] branches = TalentNode.Branch.values();
        int colW = 106;
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
        int colW = 106;
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
                    .dimensions(left + 10 + (i % 3) * 108, top + 44 + (i / 3) * 24, 104, 20)
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
        ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.patron.pick_hint").formatted(Formatting.GRAY), x, top + 30, 0xFFFFFF);
        Patron p = d.patron.isEmpty() ? null : Patron.byId(d.patron);
        int y = top + 100;
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
        bar(ctx, x + 130, y + 2, 180, 5, d.favor / (float) Patron.MAX_FAVOR, 0xFFFFC94D);
        int m2 = x + 130 + 180 * Patron.TIER2_FAVOR / Patron.MAX_FAVOR, m3 = x + 130 + 180 * Patron.TIER3_FAVOR / Patron.MAX_FAVOR;
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

    private void renderProfessions(DrawContext ctx) {
        PlayerRpgData d = data();
        int x = left + 10, y = top + 32;
        for (Profession p : Profession.values()) {
            int lvl = Professions.level(d, p), xp = Professions.xp(d, p);
            int need = lvl >= Profession.MAX_LEVEL ? 0 : Profession.xpForNext(lvl);
            ctx.drawItem(new ItemStack(Registries.ITEM.get(p.icon)), x, y + 4);
            ctx.drawTextWithShadow(textRenderer, Text.translatable(p.translationKey()).formatted(Formatting.WHITE, Formatting.BOLD)
                    .append(Text.literal("  " + lvl + "/" + Profession.MAX_LEVEL).formatted(Formatting.AQUA)), x + 22, y, 0xFFFFFF);
            bar(ctx, x + 200, y + 2, 110, 5, need > 0 ? xp / (float) need : 1f, 0xFF4DD2FF);
            String chance = fmt(Professions.perkChance(d, p) * 100) + "%";
            ctx.drawTextWithShadow(textRenderer, Text.translatable(p.translationKey() + ".perk", chance).formatted(Formatting.GRAY), x + 22, y + 11, 0xFFFFFF);
            y += 32;
        }
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
            case TAB_TALENTS -> renderTalents(ctx);
            case TAB_BLESSING -> renderBlessing(ctx);
            case TAB_PROFESSIONS -> renderProfessions(ctx);
            default -> {}
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
