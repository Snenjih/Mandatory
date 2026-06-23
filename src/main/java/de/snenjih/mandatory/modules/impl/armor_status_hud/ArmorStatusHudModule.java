package de.snenjih.mandatory.modules.impl.armor_status_hud;

import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.HudElement;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import de.snenjih.mandatory.modules.api.settings.BooleanSetting;
import de.snenjih.mandatory.modules.api.settings.IntSetting;
import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ArmorStatusHudModule extends BaseModule implements HudElement {

    private final ModuleSetting<Boolean> showOffhand;
    private final ModuleSetting<Integer> warningThreshold;

    // Display order: head first, then chest, legs, feet
    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final String[] ARMOR_LABELS = { "Head", "Chest", "Legs", "Feet" };

    public ArmorStatusHudModule() {
        super(
            "armor_status_hud",
            "Armor Status",
            "Shows armor durability on the HUD.",
            ModuleCategory.VISUAL,
            Identifier.of("mandatory", "modules/armor_status_hud")
        );
        showOffhand      = addSetting(new BooleanSetting("show_offhand",       "Show Offhand",  true));
        warningThreshold = addSetting(new IntSetting("warning_threshold", "Warning %", 20, 1, 50));
    }

    // ── HudElement ────────────────────────────────────────────────────────────

    @Override public String getHudId()      { return "armor_status_hud"; }
    @Override public String getHudName()    { return "Armor Status"; }
    @Override public int getDefaultWidth()  { return 120; }
    @Override public int getDefaultHeight() { return 70; }

    @Override
    public void renderHud(DrawContext ctx, float tickDelta, int x, int y, int w, int h) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        ClientPlayerEntity player = mc.player;
        var tr = mc.textRenderer;

        int rows = 4 + (showOffhand.get() ? 1 : 0);
        int totalH = rows * 14 + 6;

        ctx.fill(x, y, x + w, y + totalH, 0xCC0D1B2A);
        ctx.drawStrokedRectangle(x, y, w, totalH, 0xFF1E3A5F);

        int ty = y + 4;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = player.getEquippedStack(ARMOR_SLOTS[i]);
            renderArmorRow(ctx, tr, stack, ARMOR_LABELS[i], x, ty, w);
            ty += 14;
        }

        if (showOffhand.get()) {
            ItemStack offhand = player.getEquippedStack(EquipmentSlot.OFFHAND);
            renderArmorRow(ctx, tr, offhand, "Off", x, ty, w);
        }
    }

    private void renderArmorRow(DrawContext ctx, net.minecraft.client.font.TextRenderer tr,
                                 ItemStack stack, String label, int x, int ty, int w) {
        int warningPct = warningThreshold.get();

        if (stack.isEmpty()) {
            ctx.drawTextWithShadow(tr, label + ": ---", x + 4, ty + 2, 0xFF666666);
            return;
        }

        // Compute durability percent
        int maxDmg = stack.getMaxDamage();
        int color;
        int pct;
        if (maxDmg <= 0) {
            // Unbreakable item — show full
            pct = 100;
            color = 0xFF55FF55;
        } else {
            int dmg = stack.getDamage();
            pct = (int) (((float)(maxDmg - dmg) / maxDmg) * 100f);
            if (pct > 50)              color = 0xFF55FF55; // green
            else if (pct > warningPct) color = 0xFFFFFF55; // yellow
            else                       color = 0xFFFF5555; // red
        }

        // Label + percent text
        ctx.drawTextWithShadow(tr, label + ": " + pct + "%", x + 4, ty + 2, color);

        // Durability bar (60px wide, 3px tall)
        int barX = x + w - 64;
        int barY = ty + 3;
        int barW = 60;
        int barH = 3;
        int filledW = (int)(barW * pct / 100f);

        ctx.fill(barX, barY, barX + barW, barY + barH, 0xFF333333); // background
        if (filledW > 0) {
            ctx.fill(barX, barY, barX + filledW, barY + barH, color); // fill
        }
    }
}
