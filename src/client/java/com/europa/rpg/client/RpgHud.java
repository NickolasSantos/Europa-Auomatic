package com.europa.rpg.client;

import com.europa.rpg.network.RpgSyncPayload;
import com.europa.rpg.rpg.Resource;
import com.europa.rpg.talent.Talents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

/** HUD no canto superior esquerdo: classe, nível, XP, recurso da classe e recargas (R, G, V). */
public final class RpgHud {
    private RpgHud() {}

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        RpgSyncPayload d = ClientRpgState.data;
        if (d == null || mc.options.hudHidden || mc.getDebugHud().shouldShowDebugHud()) return;
        TextRenderer tr = mc.textRenderer;
        int x = 6, y = 6, w = 130;

        if ("none".equals(d.rpgClass())) {
            ctx.drawTextWithShadow(tr, Text.translatable("rpg.europa.hud.no_class"), x, y, 0xFFAAAAAA);
            return;
        }
        Text title = "none".equals(d.subclass()) ? Text.translatable("rpg.europa.class." + d.rpgClass())
                : Text.translatable("rpg.europa.subclass." + d.subclass());
        w = Math.max(w, tr.getWidth(title) + tr.getWidth("Lv " + d.level()) + 10);
        ctx.fill(x - 3, y - 3, x + w + 3, y + 50, 0x88000000);
        ctx.drawTextWithShadow(tr, title, x, y, 0xFFFFD27F);
        String lvl = "Lv " + d.level();
        ctx.drawTextWithShadow(tr, lvl, x + w - tr.getWidth(lvl), y, 0xFFFFFFFF);

        y += 11;
        float xpFrac = d.xpNeeded() > 0 ? Math.min(1f, d.xp() / (float) d.xpNeeded()) : 1f;
        bar(ctx, x, y, w, 4, xpFrac, 0xFF7CFC00, 0xFF1E3A0F);

        y += 7;
        Resource res = Resource.byId(d.resource());
        float frac = d.maxMana() > 0 ? Math.min(1f, d.mana() / d.maxMana()) : 0f;
        if (res.smallPool()) {
            int pips = (int) d.maxMana(), seg = (w - (pips - 1) * 2) / Math.max(1, pips);
            for (int i = 0; i < pips; i++) {
                int px = x + i * (seg + 2);
                ctx.fill(px, y, px + seg, y + 6, i < (int) d.mana() ? res.color : 0xFF202020);
            }
        } else {
            bar(ctx, x, y, w, 6, frac, res.color, 0xFF151515);
        }
        Text label = Text.translatable(res.translationKey()).append(" " + (int) d.mana() + "/" + (int) d.maxMana());
        ctx.drawTextWithShadow(tr, label, x + (w - tr.getWidth(label)) / 2, y - 1, 0xFFF0F0F0);

        y += 10;
        int bw = (w - 4) / 3;
        ability(ctx, tr, x, y, bw, "R", ClientRpgState.primaryCd, d.primaryCdMax(), true);
        ability(ctx, tr, x + bw + 2, y, bw, "G", ClientRpgState.ultimateCd, d.ultimateCdMax(), !"none".equals(d.subclass()));
        ability(ctx, tr, x + 2 * (bw + 2), y, bw, "V", ClientRpgState.racialCd, d.racialCdMax(), ClientRpgState.full.hasRace());
        int points = Talents.availablePoints(ClientRpgState.full);
        if (points > 0) ctx.drawTextWithShadow(tr, Text.translatable("rpg.europa.hud.points", points), x, y + 16, 0xFFFFD27F);
        if (!ClientRpgState.full.hasRace()) ctx.drawTextWithShadow(tr, Text.translatable("rpg.europa.hud.no_race"), x, y + 26, 0xFFAAAAAA);
    }

    private static void bar(DrawContext ctx, int x, int y, int w, int h, float frac, int fg, int bg) {
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y, x + Math.round(w * frac), y + h, fg);
    }

    private static void ability(DrawContext ctx, TextRenderer tr, int x, int y, int w, String key, int cd, int max, boolean unlocked) {
        int h = 12;
        ctx.fill(x, y, x + w, y + h, 0xFF202020);
        if (!unlocked) {
            ctx.drawTextWithShadow(tr, "[" + key + "] -", x + 3, y + 2, 0xFF666666);
            return;
        }
        if (cd > 0 && max > 0) {
            ctx.fill(x, y, x + Math.round(w * (1f - cd / (float) max)), y + h, 0xFF4B3A12);
            ctx.drawTextWithShadow(tr, "[" + key + "] " + String.format("%.0fs", Math.ceil(cd / 20f)), x + 3, y + 2, 0xFFBBBBBB);
        } else {
            ctx.fill(x, y, x + w, y + h, 0xFF2E6B2E);
            ctx.drawTextWithShadow(tr, "[" + key + "] OK", x + 3, y + 2, 0xFFFFFFFF);
        }
    }
}
