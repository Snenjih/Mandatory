package de.snenjih.mandatory.menu;

import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.Module;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModMenuScreen extends Screen {

    // Design colors
    private static final int COL_BG_PANEL      = 0xCC0D1B2A;
    private static final int COL_BG_CARD       = 0xCC121E30;
    private static final int COL_BORDER        = 0xFF1E3A5F;
    private static final int COL_BORDER_ACTIVE  = 0xFF4A7CF8;
    private static final int COL_TEXT_WHITE    = 0xFFFFFFFF;
    private static final int COL_TEXT_GRAY     = 0xFF8899AA;
    private static final int COL_TEXT_DIM      = 0xFF556677;
    private static final int COL_ENABLED       = 0xFF44DD88;
    private static final int COL_DISABLED      = 0xFFDD4444;
    private static final int COL_BTN_DEFAULT   = 0xFF1A2A40;
    private static final int COL_BTN_HOVER     = 0xFF243350;
    private static final int COL_ACCENT        = 0xFF4A7CF8;

    // Layout
    private static final int CARD_H    = 48;
    private static final int CARD_GAP  = 4;
    private static final int COLS      = 2;
    private static final int TAB_H     = 20;
    private static final int FOOTER_H  = 24;
    private static final int PADDING   = 8;

    private final Screen parent;

    private ModuleCategory selectedCategory = null; // null = All
    private String         searchQuery      = "";
    private boolean        searchActive     = false;
    private float          scrollOffset     = 0;

    private List<Module> visibleModules = new ArrayList<>();

    // Panel geometry (computed in render)
    private int panelX, panelY, panelW, panelH;
    private int tabBarTop, tabBarBottom, contentTop, contentBottom, footerTop;

    // Search field bounds
    private int searchX, searchY, searchW, searchH;

    // Edit HUDs button
    private int hudBtnX, hudBtnY, hudBtnW, hudBtnH;

    public ModMenuScreen(Screen parent) {
        super(Text.translatable("mandatory.modmenu.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        updateVisible();
    }

    @Override
    public boolean shouldPause() { return true; }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    // -------------------------------------------------------------------------
    // Filtering
    // -------------------------------------------------------------------------

    private void updateVisible() {
        String q = searchQuery.toLowerCase();
        visibleModules = ModuleRegistry.getInstance().getAll().stream()
                .filter(m -> selectedCategory == null || m.getCategory() == selectedCategory)
                .filter(m -> q.isEmpty()
                        || m.getName().toLowerCase().contains(q)
                        || m.getDescription().toLowerCase().contains(q))
                .collect(Collectors.toList());
        scrollOffset = 0;
    }

    // -------------------------------------------------------------------------
    // Render
    // -------------------------------------------------------------------------

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Full-screen dim
        ctx.fill(0, 0, width, height, 0x88000000);

        // Panel (80% x 85% centred)
        panelW = (int)(width  * 0.80f);
        panelH = (int)(height * 0.85f);
        panelX = (width  - panelW) / 2;
        panelY = (height - panelH) / 2;

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, COL_BG_PANEL);
        ctx.drawStrokedRectangle(panelX, panelY, panelW, panelH, COL_BORDER);

        // Tab-bar
        tabBarTop    = panelY;
        tabBarBottom = panelY + TAB_H;
        renderTabBar(ctx, mouseX, mouseY);

        // Content area
        contentTop    = tabBarBottom;
        footerTop     = panelY + panelH - FOOTER_H;
        contentBottom = footerTop;

        // Grid of module cards
        ctx.enableScissor(panelX + 1, contentTop, panelX + panelW - 1, contentBottom);
        renderModuleGrid(ctx, mouseX, mouseY);
        ctx.disableScissor();

        // Footer
        renderFooter(ctx, mouseX, mouseY);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderTabBar(DrawContext ctx, int mouseX, int mouseY) {
        // Category tabs
        ModuleCategory[] cats = ModuleCategory.values();
        int numTabs = cats.length + 1; // +1 for All

        // Reserve right side for search field
        searchW = 120;
        searchH = TAB_H - 4;
        searchX = panelX + panelW - searchW - 4;
        searchY = panelY + 2;

        int availW = searchX - panelX - 4;
        int tabW   = Math.min(60, availW / numTabs - 2);

        for (int i = 0; i < numTabs; i++) {
            ModuleCategory cat = (i == 0) ? null : cats[i - 1];
            boolean active  = (cat == selectedCategory);
            int tabX = panelX + 2 + i * (tabW + 2);
            int tabY = panelY;

            boolean hovered = mouseX >= tabX && mouseX <= tabX + tabW
                           && mouseY >= tabY && mouseY <= tabY + TAB_H;

            int bg = active  ? COL_ACCENT
                   : hovered ? COL_BTN_HOVER
                   :           COL_BTN_DEFAULT;

            ctx.fill(tabX, tabY, tabX + tabW, tabY + TAB_H, bg);
            ctx.drawStrokedRectangle(tabX, tabY, tabW, TAB_H,
                    active ? COL_BORDER_ACTIVE : COL_BORDER);

            String label = (cat == null) ? "All"
                    : cat.name().charAt(0) + cat.name().substring(1).toLowerCase();
            ctx.drawCenteredTextWithShadow(textRenderer, label,
                    tabX + tabW / 2, tabY + (TAB_H - 8) / 2,
                    active ? COL_TEXT_WHITE : COL_TEXT_GRAY);
        }

        // Search field
        ctx.fill(searchX, searchY, searchX + searchW, searchY + searchH, COL_BTN_DEFAULT);
        ctx.drawStrokedRectangle(searchX, searchY, searchW, searchH,
                searchActive ? COL_BORDER_ACTIVE : COL_BORDER);

        String display = searchQuery.isEmpty() && !searchActive
                ? "Search..."
                : (searchQuery + (searchActive && System.currentTimeMillis() % 1000 < 500 ? "|" : ""));
        int textColor = searchQuery.isEmpty() && !searchActive ? COL_TEXT_DIM : COL_TEXT_WHITE;
        ctx.drawTextWithShadow(textRenderer, display, searchX + 3, searchY + (searchH - 8) / 2, textColor);
    }

    private void renderModuleGrid(DrawContext ctx, int mouseX, int mouseY) {
        if (visibleModules.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer, "No modules found.",
                    panelX + panelW / 2, contentTop + (contentBottom - contentTop) / 2, COL_TEXT_GRAY);
            return;
        }

        int colW = (panelW - PADDING * 2 - CARD_GAP) / COLS;
        int startY = contentTop + PADDING - (int) scrollOffset;

        for (int i = 0; i < visibleModules.size(); i++) {
            int col  = i % COLS;
            int row  = i / COLS;
            int cardX = panelX + PADDING + col * (colW + CARD_GAP);
            int cardY = startY + row * (CARD_H + CARD_GAP);

            if (cardY + CARD_H < contentTop || cardY > contentBottom) continue;

            renderCard(ctx, visibleModules.get(i), cardX, cardY, colW, mouseX, mouseY);
        }
    }

    private void renderCard(DrawContext ctx, Module module, int x, int y, int w, int mx, int my) {
        boolean hovered  = mx >= x && mx <= x + w && my >= y && my <= y + CARD_H;
        boolean enabled  = module.isEnabled();

        // Card bg
        int bg = hovered ? 0xCC1A2840 : COL_BG_CARD;
        ctx.fill(x, y, x + w, y + CARD_H, bg);
        ctx.drawStrokedRectangle(x, y, w, CARD_H, enabled ? COL_ENABLED : COL_BORDER);

        // Icon (24x24)
        try {
            ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, module.getIconTexture(),
                    x + 4, y + (CARD_H - 24) / 2, 24, 24);
        } catch (Exception ignored) {}

        // Name
        ctx.drawTextWithShadow(textRenderer, module.getName(),
                x + 32, y + 10, COL_TEXT_WHITE);

        // Description (truncated)
        String desc = module.getDescription();
        if (desc.length() > 42) desc = desc.substring(0, 42) + "…";
        ctx.drawTextWithShadow(textRenderer, desc, x + 32, y + 22, COL_TEXT_GRAY);

        // Toggle dot
        int dotX = x + w - 28;
        int dotY = y + CARD_H / 2 - 4;
        ctx.fill(dotX, dotY, dotX + 8, dotY + 8, enabled ? COL_ENABLED : COL_DISABLED);
        ctx.drawStrokedRectangle(dotX, dotY, 8, 8, enabled ? 0xFF66FFAA : 0xFFFF7777);

        // Settings gear
        int gearX = x + w - 16;
        boolean hoverGear = mx >= gearX && mx <= gearX + 12 && my >= y && my <= y + CARD_H;
        ctx.drawTextWithShadow(textRenderer, "⚙",
                gearX, y + CARD_H / 2 - 4, hoverGear ? COL_ACCENT : COL_TEXT_DIM);
    }

    private void renderFooter(DrawContext ctx, int mouseX, int mouseY) {
        ctx.fill(panelX, footerTop, panelX + panelW, panelY + panelH, 0xCC0D1B2A);
        ctx.drawStrokedRectangle(panelX, footerTop, panelW, FOOTER_H, COL_BORDER);

        // Edit HUDs button
        hudBtnW = 100;
        hudBtnH = 16;
        hudBtnX = panelX + 8;
        hudBtnY = footerTop + (FOOTER_H - hudBtnH) / 2;
        boolean hoverHud = mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW
                        && mouseY >= hudBtnY && mouseY <= hudBtnY + hudBtnH;
        ctx.fill(hudBtnX, hudBtnY, hudBtnX + hudBtnW, hudBtnY + hudBtnH,
                hoverHud ? COL_BTN_HOVER : COL_BTN_DEFAULT);
        ctx.drawStrokedRectangle(hudBtnX, hudBtnY, hudBtnW, hudBtnH,
                hoverHud ? COL_BORDER_ACTIVE : COL_BORDER);
        ctx.drawCenteredTextWithShadow(textRenderer, "✦ Edit HUDs",
                hudBtnX + hudBtnW / 2, hudBtnY + (hudBtnH - 8) / 2, COL_TEXT_WHITE);

        // Module count
        String count = visibleModules.size() + " modules";
        ctx.drawTextWithShadow(textRenderer, count,
                panelX + panelW - textRenderer.getWidth(count) - 8,
                footerTop + (FOOTER_H - 8) / 2, COL_TEXT_GRAY);
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(Click click, boolean releaseOnly) {
        if (releaseOnly || click.button() != 0) return super.mouseClicked(click, releaseOnly);
        double mx = click.x(), my = click.y();

        // Tab bar
        if (my >= panelY && my <= tabBarBottom) {
            if (mx >= searchX && mx <= searchX + searchW
             && my >= searchY && my <= searchY + searchH) {
                searchActive = true;
                return true;
            }
            // Category tabs
            ModuleCategory[] cats = ModuleCategory.values();
            int numTabs = cats.length + 1;
            int availW  = searchX - panelX - 4;
            int tabW    = Math.min(60, availW / numTabs - 2);
            for (int i = 0; i < numTabs; i++) {
                int tabX = panelX + 2 + i * (tabW + 2);
                if (mx >= tabX && mx <= tabX + tabW && my >= panelY && my <= panelY + TAB_H) {
                    ModuleCategory cat = (i == 0) ? null : cats[i - 1];
                    selectedCategory = cat;
                    searchActive = false;
                    updateVisible();
                    return true;
                }
            }
        }

        // Deactivate search on click outside search field
        if (searchActive && !(mx >= searchX && mx <= searchX + searchW
                           && my >= searchY && my <= searchY + searchH)) {
            searchActive = false;
        }

        // Footer - HUD Edit button
        if (mx >= hudBtnX && mx <= hudBtnX + hudBtnW
         && my >= hudBtnY && my <= hudBtnY + hudBtnH) {
            assert client != null;
            client.setScreen(new HudEditScreen(this));
            return true;
        }

        // Module cards
        if (my >= contentTop && my <= contentBottom) {
            int colW   = (panelW - PADDING * 2 - CARD_GAP) / COLS;
            int startY = contentTop + PADDING - (int) scrollOffset;

            for (int i = 0; i < visibleModules.size(); i++) {
                int col   = i % COLS;
                int row   = i / COLS;
                int cardX = panelX + PADDING + col * (colW + CARD_GAP);
                int cardY = startY + row * (CARD_H + CARD_GAP);

                if (mx >= cardX && mx <= cardX + colW
                 && my >= cardY && my <= cardY + CARD_H) {
                    Module m = visibleModules.get(i);
                    // Gear (last 20px) → settings screen
                    if (mx >= cardX + colW - 20 && m instanceof BaseModule bm) {
                        assert client != null;
                        client.setScreen(new ModuleSettingsScreen(this, bm));
                    } else {
                        ModuleRegistry.getInstance().toggle(m);
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(click, releaseOnly);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                  double horizontalAmount, double verticalAmount) {
        if (mouseX >= panelX && mouseX <= panelX + panelW
         && mouseY >= contentTop && mouseY <= contentBottom) {
            scrollOffset -= (float) verticalAmount * 20f;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        if (searchActive) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    updateVisible();
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                searchActive = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchActive = false;
                searchQuery  = "";
                updateVisible();
                return true;
            }
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchActive && input.isValidChar()) {
            searchQuery += input.asString();
            updateVisible();
            return true;
        }
        return super.charTyped(input);
    }

    private void clampScroll() {
        int rows    = (int) Math.ceil((float) visibleModules.size() / COLS);
        int colW    = (panelW - PADDING * 2 - CARD_GAP) / COLS;
        float maxS  = Math.max(0f, rows * (CARD_H + CARD_GAP) - (contentBottom - contentTop - PADDING));
        scrollOffset = Math.clamp(scrollOffset, 0f, maxS);
    }
}
