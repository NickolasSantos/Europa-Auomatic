package com.europa.rpg.client;

import com.europa.rpg.ability.Abilities;
import com.europa.rpg.ability.AbilityDef;
import com.europa.rpg.network.RpgActionPayload;
import com.europa.rpg.rpg.RpgClass;
import com.europa.rpg.rpg.RpgSubclass;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Escolha de classe em "cartas": abas de categoria, grade de retratos, painel com retrato grande,
 * descrição, recurso, habilidade e subclasses, e o personagem do jogador em 3D (segue o mouse).
 */
public class ClassPickerScreen extends Screen {
    private static final int GOLD = 0xFFB08D4A;
    private static final int CARD_W = 58, CARD_H = 52;
    private static RpgClass.Category category = RpgClass.Category.MARTIAL;
    private static RpgClass selected;

    private int left, top, w, h, gridX, gridY, gridW, panelX;
    private ButtonWidget confirm;

    public ClassPickerScreen() {
        super(Text.translatable("rpg.europa.screen.picker_title"));
    }

    private List<RpgClass> classes() {
        return RpgClass.byCategory(category);
    }

    @Override
    protected void init() {
        w = Math.min(width - 8, 440);
        h = Math.min(height - 8, 252);
        left = (width - w) / 2;
        top = (height - h) / 2;
        int panelW = Math.min(170, w / 2 - 20);
        panelX = left + w - panelW - 8;
        gridX = left + 8;
        gridY = top + 46;
        gridW = panelX - gridX - 8;
        if (selected == null || selected.category != category) selected = classes().getFirst();

        RpgClass.Category[] cats = RpgClass.Category.values();
        int cw = (w - 16) / cats.length;
        for (int i = 0; i < cats.length; i++) {
            RpgClass.Category cat = cats[i];
            ButtonWidget b = ButtonWidget.builder(Text.translatable(cat.translationKey()), x -> {
                category = cat;
                selected = null;
                clearAndInit();
            }).dimensions(left + 8 + i * cw, top + 22, cw - 2, 18).build();
            b.active = cat != category;
            addDrawableChild(b);
        }
        confirm = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
            ClientPlayNetworking.send(new RpgActionPayload(RpgActionPayload.CHOOSE_CLASS, selected.id));
            close();
        }).dimensions(panelX, top + h - 26, w - (panelX - left) - 8, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("rpg.europa.screen.back"), b -> client.setScreen(new CharacterScreen(1)))
                .dimensions(gridX, top + h - 26, 70, 20).build());
        updateConfirm();
    }

    private void updateConfirm() {
        confirm.setMessage(Text.translatable("rpg.europa.screen.choose", Text.translatable(selected.translationKey())).formatted(Formatting.BOLD));
    }

    private int cols() {
        return Math.max(1, gridW / (CARD_W + 4));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        List<RpgClass> list = classes();
        int cols = cols();
        for (int i = 0; i < list.size(); i++) {
            int cx = gridX + (i % cols) * (CARD_W + 4), cy = gridY + (i / cols) * (CARD_H + 4);
            if (mx >= cx && mx < cx + CARD_W && my >= cy && my < cy + CARD_H) {
                selected = list.get(i);
                updateConfirm();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(left - 2, top - 2, left + w + 2, top + h + 2, GOLD);
        ctx.fill(left, top, left + w, top + h, 0xF0181410);
        ctx.fill(panelX - 4, top + 44, panelX - 3, top + h - 30, 0xFF5A4A2A);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title.copy().formatted(Formatting.GOLD, Formatting.BOLD), left + w / 2, top + 8, 0xFFFFFF);

        // cartas
        List<RpgClass> list = classes();
        int cols = cols();
        for (int i = 0; i < list.size(); i++) {
            RpgClass c = list.get(i);
            int cx = gridX + (i % cols) * (CARD_W + 4), cy = gridY + (i / cols) * (CARD_H + 4);
            boolean sel = c == selected;
            boolean hover = mouseX >= cx && mouseX < cx + CARD_W && mouseY >= cy && mouseY < cy + CARD_H;
            ctx.fill(cx, cy, cx + CARD_W, cy + CARD_H, sel ? 0xFFE0B860 : hover ? 0xFF9A8A6A : 0xFF3A3020);
            ctx.fill(cx + 1, cy + 1, cx + CARD_W - 1, cy + CARD_H - 1, sel ? 0xFF3A2E1A : 0xFF201A12);
            Portraits.draw(ctx, c, cx + (CARD_W - 32) / 2, cy + 3, 32);
            Text name = Text.translatable(c.translationKey());
            String s = textRenderer.trimToWidth(name.getString(), CARD_W - 4);
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(s).styled(st -> st.withColor(c.color)), cx + CARD_W / 2, cy + 39, 0xFFFFFF);
        }

        // painel do selecionado
        RpgClass c = selected;
        int px = panelX, py = top + 46, pw = w - (panelX - left) - 8;
        ctx.fill(px - 1, py - 1, px + 49, py + 49, GOLD);
        Portraits.draw(ctx, c, px, py, 48);
        ctx.drawTextWithShadow(textRenderer, Text.translatable(c.translationKey()).styled(st -> st.withColor(c.color).withBold(true)), px + 54, py + 2, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.translatable(c.category.translationKey()).formatted(Formatting.GRAY), px + 54, py + 13, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.translatable(c.resource.translationKey()), px + 54, py + 24, c.resource.color);
        int ty = py + 54;
        for (OrderedText l : textRenderer.wrapLines(Text.translatable(c.translationKey() + ".desc").formatted(Formatting.GRAY), pw)) {
            ctx.drawTextWithShadow(textRenderer, l, px, ty, 0xFFFFFF);
            ty += 9;
        }
        ty += 3;
        AbilityDef def = Abilities.get(c.primaryAbility);
        MutableText ab = Text.literal("[R] ").formatted(Formatting.YELLOW).append(Text.translatable("rpg.europa.ability." + c.primaryAbility).formatted(Formatting.WHITE));
        ctx.drawTextWithShadow(textRenderer, ab, px, ty, 0xFFFFFF);
        ty += 11;
        ctx.drawTextWithShadow(textRenderer, Text.translatable("rpg.europa.screen.subclasses").formatted(Formatting.LIGHT_PURPLE), px, ty, 0xFFFFFF);
        ty += 10;
        int modelLeft = px + pw - 46;
        for (RpgSubclass sc : c.subclasses()) {
            String n = textRenderer.trimToWidth(Text.translatable(sc.translationKey()).getString(), modelLeft - px - 6);
            ctx.drawTextWithShadow(textRenderer, Text.literal("• " + n).styled(st -> st.withColor(sc.color)), px, ty, 0xFFFFFF);
            ty += 10;
        }

        // personagem 3D (gira com o mouse)
        if (client != null && client.player != null) {
            int mx1 = modelLeft, my1 = top + h - 96, mx2 = px + pw, my2 = top + h - 30;
            InventoryScreen.drawEntity(ctx, mx1, my1 - 10, mx2, my2, 38, 0.0625f, mouseX, mouseY, client.player);
        }

        // tooltip com descrição da habilidade ao passar sobre a linha [R]
        if (def != null && mouseX >= px && mouseX < px + pw && mouseY >= ty - 10 * (c.subclasses().size() + 2) - 1 && mouseY < ty - 10 * (c.subclasses().size() + 1)) {
            ctx.drawOrderedTooltip(textRenderer, textRenderer.wrapLines(def.description().get(), 200), mouseX, mouseY);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
