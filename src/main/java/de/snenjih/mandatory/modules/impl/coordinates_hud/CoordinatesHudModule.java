package de.snenjih.mandatory.modules.impl.coordinates_hud;

import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.HudElement;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import de.snenjih.mandatory.modules.api.settings.BooleanSetting;
import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

public class CoordinatesHudModule extends BaseModule implements HudElement {

    private final ModuleSetting<Boolean> showDirection;
    private final ModuleSetting<Boolean> showBiome;
    private final ModuleSetting<Boolean> compactMode;

    public CoordinatesHudModule() {
        super(
            "coordinates_hud",
            "Coordinates HUD",
            "Displays your X/Y/Z coordinates on the HUD.",
            ModuleCategory.VISUAL,
            Identifier.of("mandatory", "modules/coordinates_hud")
        );
        showDirection = addSetting(new BooleanSetting("show_direction", "Show Direction", true));
        showBiome     = addSetting(new BooleanSetting("show_biome",     "Show Biome",     false));
        compactMode   = addSetting(new BooleanSetting("compact_mode",   "Compact Mode",   false));
    }

    // ── HudElement ────────────────────────────────────────────────────────────

    @Override public String getHudId()      { return "coordinates_hud"; }
    @Override public String getHudName()    { return "Coordinates HUD"; }
    @Override public int getDefaultWidth()  { return 130; }
    @Override public int getDefaultHeight() { return 40; }

    @Override
    public void renderHud(DrawContext ctx, float tickDelta, int x, int y, int w, int h) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        ClientPlayerEntity player = mc.player;
        var tr = mc.textRenderer;

        // Compute effective height
        int extraH = 0;
        if (showBiome.get()) extraH = 10;
        int totalH = compactMode.get() ? 18 : (h + extraH);

        ctx.fill(x, y, x + w, y + totalH, 0xCC0D1B2A);
        ctx.drawStrokedRectangle(x, y, w, totalH, 0xFF1E3A5F);

        int bx = (int) player.getX();
        int by = (int) player.getY();
        int bz = (int) player.getZ();

        if (compactMode.get()) {
            String text = bx + " / " + by + " / " + bz;
            ctx.drawTextWithShadow(tr, text, x + 4, y + (totalH - 8) / 2, 0xFFFFFFFF);
        } else {
            ctx.drawTextWithShadow(tr, "X: " + bx, x + 4, y + 5,  0xFFFFFFFF);
            ctx.drawTextWithShadow(tr, "Y: " + by, x + 4, y + 14, 0xFFFFFFFF);
            ctx.drawTextWithShadow(tr, "Z: " + bz, x + 4, y + 23, 0xFFFFFFFF);

            if (showDirection.get()) {
                String dir = player.getHorizontalFacing().asString().toUpperCase();
                ctx.drawTextWithShadow(tr, dir, x + w - tr.getWidth(dir) - 4, y + 14, 0xFF8899AA);
            }
        }

        if (showBiome.get() && mc.world != null) {
            try {
                var biomeEntry = mc.world.getBiome(player.getBlockPos());
                String biomeName = biomeEntry.getKey()
                        .map(k -> k.getValue().getPath())
                        .orElse("unknown");
                biomeName = biomeName.replace('_', ' ');
                if (!biomeName.isEmpty()) {
                    biomeName = Character.toUpperCase(biomeName.charAt(0)) + biomeName.substring(1);
                }
                int biomeY = compactMode.get() ? y + totalH + 2 : y + h - 10;
                ctx.drawTextWithShadow(tr, biomeName, x + 4, biomeY, 0xFF8899AA);
            } catch (Exception ignored) {}
        }
    }
}
