package de.snenjih.mandatory.menu;

import de.snenjih.mandatory.modules.api.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public final class CarouselRenderer {

    public static final int CARD_WIDTH   = 160;
    public static final int CARD_HEIGHT  = 200;
    private static final int ICON_SIZE   = 64;
    public static final int CARD_SPACING = 180;

    private static final int COLOR_TITLE    = 0xFFFFFFFF;
    private static final int COLOR_DESC     = 0xFFAAAAAA;
    private static final int COLOR_ENABLED  = 0xFF44FF88;
    private static final int COLOR_DISABLED = 0xFFFF5555;
    private static final int COLOR_BTN_ON   = 0xFF22AA55;
    private static final int COLOR_BTN_OFF  = 0xFF883333;

    public void render(DrawContext ctx, List<Module> modules, float scrollOffset,
                       int screenWidth, int screenHeight, float tickDelta) {
        if (modules.isEmpty()) return;

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        for (int i = 0; i < modules.size(); i++) {
            float cardCenterX = centerX + i * CARD_SPACING - scrollOffset;
            float dist        = Math.abs(cardCenterX - centerX);
            if (dist > screenWidth) continue;

            float scale = Math.max(0.72f, 1.0f - dist / (CARD_SPACING * 3.0f));
            int   alpha = (int) (255 * Math.max(0.25f, scale));

            renderCard(ctx, tr, modules.get(i), (int) cardCenterX, centerY, scale, alpha);
        }

        renderArrows(ctx, tr, screenWidth, screenHeight, modules.size(), scrollOffset);
    }

    private void renderCard(DrawContext ctx, TextRenderer tr, Module module,
                             int cx, int cy, float scale, int alpha) {
        int w = (int) (CARD_WIDTH  * scale);
        int h = (int) (CARD_HEIGHT * scale);
        int x = cx - w / 2;
        int y = cy - h / 2;

        ctx.fill(x, y, x + w, y + h, (alpha << 24) | 0x16213E);
        ctx.drawStrokedRectangle(x, y, w, h, (alpha << 24) | 0x4A4A7E);

        // Icon
        int iconW = (int) (ICON_SIZE * scale);
        int iconX = cx - iconW / 2;
        int iconY = y + (int) (18 * scale);

        Identifier icon = module.getIconTexture();
        ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY, iconW, iconW);

        // Status dot
        int dotR = (int) (6 * scale);
        int dotColor = module.isEnabled() ? COLOR_ENABLED : COLOR_DISABLED;
        int dotX = cx - dotR;
        int dotY = iconY + iconW + (int) (6 * scale);
        ctx.fill(dotX, dotY, dotX + dotR * 2, dotY + dotR * 2,
                 (alpha << 24) | (dotColor & 0x00FFFFFF));

        // Module name
        int nameY = dotY + dotR * 2 + (int) (8 * scale);
        ctx.drawCenteredTextWithShadow(tr, module.getName(), cx, nameY,
                                       (alpha << 24) | (COLOR_TITLE & 0x00FFFFFF));

        // Short description
        String desc = module.getDescription();
        if (desc.length() > 28) desc = desc.substring(0, 26) + "…";
        ctx.drawCenteredTextWithShadow(tr, desc, cx, nameY + 12,
                                       (alpha << 24) | (COLOR_DESC & 0x00FFFFFF));
    }

    public void renderToggleButton(DrawContext ctx, Module module, int sw, int sh) {
        if (module == null) return;
        TextRenderer tr  = MinecraftClient.getInstance().textRenderer;
        int btnW = 120;
        int btnH = 20;
        int btnX = sw / 2 - btnW / 2;
        int btnY = sh / 2 + CARD_HEIGHT / 2 + 10;

        ctx.fill(btnX, btnY, btnX + btnW, btnY + btnH,
                 module.isEnabled() ? COLOR_BTN_ON : COLOR_BTN_OFF);
        ctx.drawStrokedRectangle(btnX, btnY, btnW, btnH, 0xFF000000);

        ctx.drawCenteredTextWithShadow(tr,
            Text.literal(module.isEnabled() ? "Disable" : "Enable"),
            sw / 2, btnY + 6, COLOR_TITLE);
    }

    private void renderArrows(DrawContext ctx, TextRenderer tr,
                               int sw, int sh, int count, float offset) {
        if (count <= 1) return;
        int cy = sh / 2;
        if (offset > 1) {
            ctx.drawCenteredTextWithShadow(tr, Text.literal("◀"),
                                           sw / 2 - CARD_WIDTH, cy, 0xFFCCCCCC);
        }
        if (offset < (count - 1) * CARD_SPACING - 1) {
            ctx.drawCenteredTextWithShadow(tr, Text.literal("▶"),
                                           sw / 2 + CARD_WIDTH, cy, 0xFFCCCCCC);
        }
    }

    public static int getCardSpacing() { return CARD_SPACING; }
    public static int getCardHeight()  { return CARD_HEIGHT;  }
    public static int getCardWidth()   { return CARD_WIDTH;   }
}
