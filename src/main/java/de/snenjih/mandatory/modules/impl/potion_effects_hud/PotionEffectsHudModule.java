package de.snenjih.mandatory.modules.impl.potion_effects_hud;

import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.HudElement;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import de.snenjih.mandatory.modules.api.settings.BooleanSetting;
import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PotionEffectsHudModule extends BaseModule implements HudElement {

    private final ModuleSetting<Boolean> showDuration;
    private final ModuleSetting<Boolean> compactMode;

    public PotionEffectsHudModule() {
        super(
            "potion_effects_hud",
            "Potion Effects",
            "Displays active potion effects on the HUD.",
            ModuleCategory.VISUAL,
            Identifier.of("mandatory", "modules/potion_effects_hud")
        );
        showDuration = addSetting(new BooleanSetting("show_duration", "Show Duration", true));
        compactMode  = addSetting(new BooleanSetting("compact_mode",  "Compact Mode",  false));
    }

    // ── HudElement ────────────────────────────────────────────────────────────

    @Override public String getHudId()      { return "potion_effects_hud"; }
    @Override public String getHudName()    { return "Potion Effects"; }
    @Override public int getDefaultWidth()  { return 140; }
    @Override public int getDefaultHeight() { return 20; }

    @Override
    public void renderHud(DrawContext ctx, float tickDelta, int x, int y, int w, int h) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        ClientPlayerEntity player = mc.player;
        var tr = mc.textRenderer;

        List<StatusEffectInstance> effects = new ArrayList<>(player.getStatusEffects());
        if (effects.isEmpty()) return;

        // Sort: beneficial first, then harmful; within each group, sort by remaining duration desc
        effects.sort(Comparator
                .<StatusEffectInstance, Boolean>comparing(e -> !e.getEffectType().value().isBeneficial())
                .thenComparing(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed()));

        int lineH = compactMode.get() ? 10 : 18;
        int totalH = Math.max(h, effects.size() * lineH + 4);

        ctx.fill(x, y, x + w, y + totalH, 0xCC0D1B2A);
        ctx.drawStrokedRectangle(x, y, w, totalH, 0xFF1E3A5F);

        for (int i = 0; i < effects.size(); i++) {
            StatusEffectInstance eff = effects.get(i);
            int lineY = y + 3 + i * lineH;

            boolean beneficial = eff.getEffectType().value().isBeneficial();
            int nameColor = beneficial ? 0xFF55FF55 : 0xFFFF5555;

            String name = eff.getEffectType().value().getName().getString();
            int durationSecs = eff.getDuration() / 20;

            if (compactMode.get()) {
                // Compact: first char of name + duration
                String initial = name.isEmpty() ? "?" : String.valueOf(Character.toUpperCase(name.charAt(0)));
                String label = showDuration.get()
                        ? initial + ":" + durationSecs + "s"
                        : initial;
                ctx.drawTextWithShadow(tr, label, x + 4 + i * 28, y + 3, nameColor);
            } else {
                String durationStr = showDuration.get() ? " " + durationSecs + "s" : "";
                ctx.drawTextWithShadow(tr, name, x + 4, lineY, nameColor);
                if (showDuration.get()) {
                    String ds = durationSecs + "s";
                    ctx.drawTextWithShadow(tr, ds, x + w - tr.getWidth(ds) - 4, lineY, 0xFF8899AA);
                }
            }
        }
    }
}
