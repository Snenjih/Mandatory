package de.snenjih.mandatory.menu;

import de.snenjih.mandatory.modules.api.Module;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class CarouselScreen extends Screen {

    private final Screen parent;
    private final List<Module> allModules;
    private final CarouselRenderer renderer;

    private List<Module> visibleModules;
    private ModuleCategory selectedCategory = null; // null = All

    private float scrollOffset   = 0f;
    private float scrollVelocity = 0f;
    private boolean isDragging   = false;

    private static final float FRICTION     = 0.85f;
    private static final float SNAP_SPEED   = 0.15f;
    private static final int   CARD_SPACING = CarouselRenderer.getCardSpacing();

    // Tab layout
    private static final int TAB_H      = 14;
    private static final int TAB_W      = 58;
    private static final int TAB_GAP    = 4;
    private static final int TAB_TOP    = 34;

    public CarouselScreen(Screen parent) {
        super(Text.translatable("mandatory.menu.title"));
        this.parent      = parent;
        this.allModules  = ModuleRegistry.getInstance().getAll();
        this.renderer    = new CarouselRenderer();
        this.visibleModules = new ArrayList<>(allModules);
    }

    // -------------------------------------------------------------------------
    // Filtering
    // -------------------------------------------------------------------------

    private void selectCategory(ModuleCategory category) {
        selectedCategory  = category;
        scrollOffset      = 0f;
        scrollVelocity    = 0f;
        if (category == null) {
            visibleModules = new ArrayList<>(allModules);
        } else {
            visibleModules = allModules.stream()
                    .filter(m -> m.getCategory() == category)
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    // -------------------------------------------------------------------------
    // Tab geometry helpers
    // -------------------------------------------------------------------------

    private int tabCount() {
        return ModuleCategory.values().length + 1; // +1 for "All"
    }

    private int totalTabWidth() {
        return tabCount() * TAB_W + (tabCount() - 1) * TAB_GAP;
    }

    private int tabStartX() {
        return width / 2 - totalTabWidth() / 2;
    }

    private int tabX(int idx) {
        return tabStartX() + idx * (TAB_W + TAB_GAP);
    }

    private int clickedTabIndex(double mx, double my) {
        if (my < TAB_TOP || my > TAB_TOP + TAB_H) return -1;
        for (int i = 0; i < tabCount(); i++) {
            int x = tabX(i);
            if (mx >= x && mx <= x + TAB_W) return i;
        }
        return -1;
    }

    // -------------------------------------------------------------------------
    // Screen lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        scrollVelocity *= FRICTION;
        scrollOffset   += scrollVelocity;

        float maxOffset = Math.max(0, (visibleModules.size() - 1) * CARD_SPACING);
        scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset));

        if (Math.abs(scrollVelocity) < 0.5f) {
            int   targetIndex  = Math.round(scrollOffset / CARD_SPACING);
            float targetOffset = targetIndex * CARD_SPACING;
            scrollOffset += (targetOffset - scrollOffset) * SNAP_SPEED;
            if (Math.abs(scrollOffset - targetOffset) < 0.5f) {
                scrollOffset  = targetOffset;
                scrollVelocity = 0;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatable("mandatory.menu.title"),
            width / 2, 16, 0xFFFFFFFF
        );

        renderCategoryTabs(context, mouseX, mouseY);

        if (visibleModules.isEmpty()) {
            context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("No modules in this category."),
                width / 2, height / 2, 0xFF888888
            );
        } else {
            Module active = getActiveModule();
            renderer.render(context, visibleModules, scrollOffset, width, height, delta);
            renderer.renderToggleButton(context, active, width, height);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderCategoryTabs(DrawContext ctx, int mouseX, int mouseY) {
        ModuleCategory[] cats = ModuleCategory.values();

        // idx 0 = All, idx 1..n = categories
        for (int i = 0; i < tabCount(); i++) {
            ModuleCategory cat = (i == 0) ? null : cats[i - 1];
            boolean active     = (cat == selectedCategory);
            boolean hovered    = (mouseX >= tabX(i) && mouseX <= tabX(i) + TAB_W
                                && mouseY >= TAB_TOP && mouseY <= TAB_TOP + TAB_H);

            int bg = active  ? 0xFF4A4A7E
                   : hovered ? 0xFF2A2A4E
                   :           0xFF16213E;

            ctx.fill(tabX(i), TAB_TOP, tabX(i) + TAB_W, TAB_TOP + TAB_H, bg);
            ctx.drawStrokedRectangle(tabX(i), TAB_TOP, TAB_W, TAB_H,
                    active ? 0xFF8888CC : 0xFF333366);

            String label = (cat == null) ? "All"
                    : cat.name().charAt(0) + cat.name().substring(1).toLowerCase();
            ctx.drawCenteredTextWithShadow(
                    textRenderer, label,
                    tabX(i) + TAB_W / 2, TAB_TOP + 3,
                    active ? 0xFFFFFFFF : 0xFFAAAAAA
            );
        }
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(Click click, boolean releaseOnly) {
        if (click.button() == 0 && !releaseOnly) {
            int tabIdx = clickedTabIndex(click.x(), click.y());
            if (tabIdx >= 0) {
                ModuleCategory[] cats = ModuleCategory.values();
                selectCategory(tabIdx == 0 ? null : cats[tabIdx - 1]);
                return true;
            }

            isDragging = true;

            Module active = getActiveModule();
            if (active != null && isOverToggleButton(click.x(), click.y())) {
                ModuleRegistry.getInstance().toggle(active);
                return true;
            }
        }
        return super.mouseClicked(click, releaseOnly);
    }

    @Override
    public boolean mouseReleased(Click click) {
        isDragging = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (click.button() == 0 && isDragging) {
            scrollVelocity -= (float) deltaX * 0.4f;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Module getActiveModule() {
        if (visibleModules.isEmpty()) return null;
        int index = Math.round(scrollOffset / CARD_SPACING);
        index = Math.max(0, Math.min(visibleModules.size() - 1, index));
        return visibleModules.get(index);
    }

    private boolean isOverToggleButton(double mouseX, double mouseY) {
        int btnW = 120;
        int btnH = 20;
        int btnX = width / 2 - btnW / 2;
        int btnY = height / 2 + CarouselRenderer.getCardHeight() / 2 + 10;
        return mouseX >= btnX && mouseX <= btnX + btnW
            && mouseY >= btnY && mouseY <= btnY + btnH;
    }
}
