package de.snenjih.mandatory.modules.impl.fps_ping_hud;

import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.HudElement;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import de.snenjih.mandatory.modules.api.settings.BooleanSetting;
import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;

public class FpsPingHudModule extends BaseModule implements HudElement {

    private final ModuleSetting<Boolean> showPing;
    private final ModuleSetting<Boolean> colorCode;

    public FpsPingHudModule() {
        super(
            "fps_ping_hud",
            "FPS / Ping",
            "Displays FPS and ping on the HUD.",
            ModuleCategory.VISUAL,
            Identifier.of("mandatory", "modules/fps_ping_hud")
        );
        showPing  = addSetting(new BooleanSetting("show_ping",   "Show Ping",   true));
        colorCode = addSetting(new BooleanSetting("color_code",  "Color Code",  true));
    }

    // ── HudElement ────────────────────────────────────────────────────────────

    @Override public String getHudId()      { return "fps_ping_hud"; }
    @Override public String getHudName()    { return "FPS / Ping"; }
    @Override public int getDefaultWidth()  { return 100; }
    @Override public int getDefaultHeight() { return 18; }

    @Override
    public void renderHud(DrawContext ctx, float tickDelta, int x, int y, int w, int h) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        var tr = mc.textRenderer;

        ctx.fill(x, y, x + w, y + h, 0xCC0D1B2A);
        ctx.drawStrokedRectangle(x, y, w, h, 0xFF1E3A5F);

        int fps = mc.getCurrentFps();

        int fpsColor = 0xFFFFFFFF;
        if (colorCode.get()) {
            if (fps > 40)      fpsColor = 0xFF55FF55; // green
            else if (fps > 20) fpsColor = 0xFFFFFF55; // yellow
            else               fpsColor = 0xFFFF5555; // red
        }

        // Resolve ping
        int ping = -1;
        if (showPing.get() && mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler()
                    .getPlayerListEntry(mc.player.getGameProfile().id());
            if (entry != null) ping = entry.getLatency();
        }

        String text;
        if (showPing.get() && ping >= 0) {
            int pingColor = 0xFFFFFFFF;
            if (colorCode.get()) {
                if (ping < 50)       pingColor = 0xFF55FF55;
                else if (ping < 100) pingColor = 0xFFFFFF55;
                else                 pingColor = 0xFFFF5555;
            }
            // Render "60 FPS | 42ms" with separate colors per segment
            int ty = y + (h - 8) / 2;
            String fpsPart  = fps + " FPS | ";
            String pingPart = ping + "ms";
            ctx.drawTextWithShadow(tr, fpsPart,  x + 4, ty, fpsColor);
            ctx.drawTextWithShadow(tr, pingPart, x + 4 + tr.getWidth(fpsPart), ty, pingColor);
        } else {
            String fpsPart = fps + " FPS";
            ctx.drawTextWithShadow(tr, fpsPart, x + 4, y + (h - 8) / 2, fpsColor);
        }
    }
}
