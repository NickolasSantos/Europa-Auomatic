package com.europa.rpg.client;

import com.europa.rpg.rpg.RpgClass;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/** Atlas de retratos das classes (gerado por tools/portraits.py na ordem de RpgClass.all()). */
public final class Portraits {
    private static final Identifier ATLAS = Identifier.of("europa", "textures/gui/portraits.png");

    private Portraits() {}

    public static void draw(DrawContext ctx, RpgClass c, int x, int y, int size) {
        int idx = RpgClass.all().indexOf(c);
        if (idx < 0) return;
        ctx.drawTexture(ATLAS, x, y, size, size, (idx % 8) * 32, (idx / 8) * 32, 32, 32, 256, 128);
    }
}
