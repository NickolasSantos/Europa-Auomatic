package com.europa.rpg.client;

import com.europa.rpg.network.RpgSyncPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

/** HUD no canto superior esquerdo: classe, nível, XP, mana e recargas. */
public final class RpgHud {
    private RpgHud() {}

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        RpgSyncPayload d = ClientRpgState.data;
        if (d == null || mc.options.hudHidden || mc.getDebugHud().shouldShowDebugHud()) return;
        TextRenderer tr = mc.textRenderer;
        int x = 6, y = 6, w = 102;

        if ("none".equals(d.rpgClass())) {
            ctx.drawTextWithShadow(tr, Text.translatable("rpg.europa.hud.no_class"), x, y, 0xFFAAAAAA);
            return;
        }

        ctx.fill(x - 3, y - 3, x + w + 3, y + 50, 0x88000000);
        Text title = Text.translatable("rpg.europa.class." + d.rpgClass());
        if (!"none".equals(d.subclass())) {
            title = Text.translatable("rpg.europa.subclass." + d.subclass());
        }
        ctx.drawTextWithShadow(tr, title, x, y, 0xFFFFD27F);
        String lvl = "Lv " + d.level();
        ctx.drawTextWithShadow(tr, lvl, x + w - tr.getWidth(lvl), y, 0xFFFFFFFF);

        // XP
        y += 11;
        float xpFrac = d.xpNeeded() > 0 ? Math.min(1f, d.xp() / (float) d.xpNeeded()) : 1f;
        bar(ctx, x, y, w, 4, xpFrac, 0xFF7CFC00, 0xFF1E3A0F);
        // Mana
        y += 7;
        float manaFrac = d.maxMana() > 0 ? Math.min(1f, d.mana() / d.maxMana()) : 0f;
        bar(ctx, x, y, w, 6, manaFrac, 0xFF3B82F6, 0xFF0F1E3A);
        String mana = (int) d.mana() + "/" + (int) d.maxMana();
        ctx.drawTextWithShadow(tr, mana, x + (w - tr.getWidth(mana)) / 2, y - 1, 0xFFE0E8FF);

        // Recargas
        y += 10;
        ability(ctx, tr, x, y, "R", ClientRpgState.primaryCd, d.primaryCdMax(), true);
        ability(ctx, tr, x + w / 2 + 2, y, "G", ClientRpgState.ultimateCd, d.ultimateCdMax(), !"none".equals(d.subclass()));
    }

    private static void bar(DrawContext ctx, int x, int y, int w, int h, float frac, int fg, int bg) {
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y, x + Math.round(w * frac), y + h, fg);
    }

    private static void ability(DrawContext ctx, TextRenderer tr, int x, int y, String key, int cd, int max, boolean unlocked) {
        int w = 48, h = 12;
        ctx.fill(x, y, x + w, y + h, 0xFF202020);
        if (!unlocked) {
            ctx.drawTextWithShadow(tr, "[" + key + "] -", x + 3, y + 2, 0xFF666666);
            return;
        }
        if (cd > 0 && max > 0) {
            ctx.fill(x, y, x + Math.round(w * (1f - cd / (float) max)), y + h, 0xFF4B3A12);
            ctx.drawTextWithShadow(tr, "[" + key + "] " + String.format("%.1fs", cd / 20f), x + 3, y + 2, 0xFFBBBBBB);
        } else {
            ctx.fill(x, y, x + w, y + h, 0xFF2E6B2E);
            ctx.drawTextWithShadow(tr, "[" + key + "] OK", x + 3, y + 2, 0xFFFFFFFF);
        }
    }
}
