package com.europa.rpg.client;

import com.europa.rpg.network.RpgSyncPayload;
import com.europa.rpg.race.Race;
import com.europa.rpg.race.Races;
import com.europa.rpg.rpg.Resource;
import com.europa.rpg.rpg.RpgClass;
import com.europa.rpg.talent.Talents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * HUD: painel do personagem (rosto do jogador + retrato da classe, nível, recurso e XP) no canto superior
 * esquerdo e três espaços de habilidade (R, G, V) ao lado da barra de itens, com recarga visual.
 */
public final class RpgHud {
    private static final int GOLD = 0xFFB08D4A, PANEL = 0xC0141016;

    private RpgHud() {}

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        RpgSyncPayload d = ClientRpgState.data;
        if (d == null || mc.player == null || mc.options.hudHidden || mc.getDebugHud().shouldShowDebugHud()) return;
        TextRenderer tr = mc.textRenderer;
        RpgClass cls = RpgClass.byId(d.rpgClass());

        // ---------------- painel do personagem
        int x = 4, y = 4, w = 156, h = 44;
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, GOLD);
        ctx.fill(x, y, x + w, y + h, PANEL);
        ctx.fill(x + 3, y + 3, x + 31, y + 31, GOLD);
        PlayerSkinDrawer.draw(ctx, mc.player.getSkinTextures(), x + 4, y + 4, 26);
        if (cls != RpgClass.NONE) {
            ctx.fill(x + 20, y + 20, x + 37, y + 37, GOLD);
            Portraits.draw(ctx, cls, x + 21, y + 21, 15);
        }
        int lvlW = 22;
        ctx.fill(x + 3, y + 33, x + 3 + lvlW, y + 42, 0xFF2A2018);
        ctx.drawCenteredTextWithShadow(tr, String.valueOf(d.level()), x + 3 + lvlW / 2, y + 34, 0xFFFFD27F);

        int tx = x + 40, tw = w - 44;
        if (cls == RpgClass.NONE) {
            ctx.drawTextWithShadow(tr, Text.translatable("rpg.europa.hud.no_class"), tx, y + 6, 0xFFDDDDDD);
            ctx.drawTextWithShadow(tr, Text.translatable("rpg.europa.hud.press_k"), tx, y + 18, 0xFF999999);
            return;
        }
        Text title = "none".equals(d.subclass()) ? Text.translatable(cls.translationKey())
                : Text.translatable("rpg.europa.subclass." + d.subclass());
        ctx.drawTextWithShadow(tr, title.copy().styled(s -> s.withColor(cls.color)), tx, y + 4, 0xFFFFFFFF);

        Resource res = Resource.byId(d.resource());
        int by = y + 15;
        if (res.smallPool()) {
            int pips = (int) d.maxMana(), seg = (tw - (pips - 1) * 2) / Math.max(1, pips);
            for (int i = 0; i < pips; i++) {
                int px = tx + i * (seg + 2);
                boolean on = i < (int) d.mana();
                ctx.fill(px, by, px + seg, by + 10, 0xFF101010);
                if (on) {
                    ctx.fill(px + 1, by + 1, px + seg - 1, by + 9, res.color);
                    ctx.fill(px + 1, by + 1, px + seg - 1, by + 3, brighten(res.color));
                }
            }
        } else {
            float frac = d.maxMana() > 0 ? Math.min(1f, d.mana() / d.maxMana()) : 0f;
            ctx.fill(tx, by, tx + tw, by + 10, 0xFF101010);
            int fw = Math.round((tw - 2) * frac);
            ctx.fill(tx + 1, by + 1, tx + 1 + fw, by + 9, res.color);
            ctx.fill(tx + 1, by + 1, tx + 1 + fw, by + 3, brighten(res.color));
        }
        Text label = Text.translatable(res.translationKey()).append(" " + (int) d.mana() + "/" + (int) d.maxMana());
        ctx.drawTextWithShadow(tr, label, tx + (tw - tr.getWidth(label)) / 2, by + 1, 0xFFFFFFFF);

        int xy = y + 29;
        float xpFrac = d.xpNeeded() > 0 ? Math.min(1f, d.xp() / (float) d.xpNeeded()) : 1f;
        ctx.fill(tx, xy, tx + tw, xy + 4, 0xFF101010);
        ctx.fill(tx + 1, xy + 1, tx + 1 + Math.round((tw - 2) * xpFrac), xy + 3, 0xFF7CFC00);
        String xpText = d.xpNeeded() > 0 ? Math.round(xpFrac * 100) + "% XP" : "MAX";
        ctx.drawTextWithShadow(tr, xpText, tx + tw - tr.getWidth(xpText), xy + 6, 0xFF9AD97A);

        int points = Talents.availablePoints(ClientRpgState.full);
        int ny = y + h + 4;
        if (points > 0) {
            ctx.drawTextWithShadow(tr, Text.translatable("rpg.europa.hud.points", points), x, ny, 0xFFFFD27F);
            ny += 10;
        }
        if (!ClientRpgState.full.hasRace()) ctx.drawTextWithShadow(tr, Text.translatable("rpg.europa.hud.no_race"), x, ny, 0xFFAAAAAA);

        // ---------------- habilidades ao lado da barra de itens
        int sx = ctx.getScaledWindowWidth() / 2 + 91 + 8, sy = ctx.getScaledWindowHeight() - 23;
        Race race = Races.of(ClientRpgState.full);
        slot(ctx, tr, sx, sy, iconOf(cls.icon), EuropaRPGClient.primaryKey, ClientRpgState.primaryCd, d.primaryCdMax(), true);
        slot(ctx, tr, sx + 24, sy, new ItemStack(Items.NETHER_STAR), EuropaRPGClient.ultimateKey, ClientRpgState.ultimateCd,
                d.ultimateCdMax(), !"none".equals(d.subclass()));
        slot(ctx, tr, sx + 48, sy, race == null ? new ItemStack(Items.PLAYER_HEAD) : iconOf(race.icon()), EuropaRPGClient.racialKey,
                ClientRpgState.racialCd, d.racialCdMax(), race != null);
    }

    private static ItemStack iconOf(String id) {
        return new ItemStack(Registries.ITEM.get(Identifier.of(id)));
    }

    private static void slot(DrawContext ctx, TextRenderer tr, int x, int y, ItemStack icon, KeyBinding key, int cd, int max, boolean unlocked) {
        boolean ready = unlocked && cd <= 0;
        ctx.fill(x - 1, y - 1, x + 23, y + 23, ready ? 0xFFE0B860 : 0xFF3A3020);
        ctx.fill(x, y, x + 22, y + 22, 0xE0141016);
        ctx.drawItem(icon, x + 3, y + 3);
        if (!unlocked) {
            ctx.fill(x, y, x + 22, y + 22, 0xB0000000);
        } else if (cd > 0 && max > 0) {
            int hh = Math.round(22 * Math.min(1f, cd / (float) max));
            ctx.fill(x, y, x + 22, y + hh, 0xA0000000);
            String s = String.valueOf((int) Math.ceil(cd / 20f));
            ctx.getMatrices().push();
            ctx.getMatrices().translate(0, 0, 200);
            ctx.drawCenteredTextWithShadow(tr, s, x + 11, y + 7, 0xFFFFFFFF);
            ctx.getMatrices().pop();
        }
        String k = key.getBoundKeyLocalizedText().getString();
        if (k.length() > 2) k = k.substring(0, 2);
        ctx.getMatrices().push();
        ctx.getMatrices().translate(0, 0, 200);
        ctx.drawTextWithShadow(tr, k, x + 22 - tr.getWidth(k), y + 14, unlocked ? 0xFFFFD27F : 0xFF777777);
        ctx.getMatrices().pop();
    }

    private static int brighten(int argb) {
        int r = Math.min(255, ((argb >> 16) & 0xFF) + 70), g = Math.min(255, ((argb >> 8) & 0xFF) + 70), b = Math.min(255, (argb & 0xFF) + 70);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
