package de.snenjih.mandatory.menu;

import de.snenjih.mandatory.modules.api.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class CarouselScreen extends Screen {

    private final Screen parent;
    private final List<Module> modules;
    private final CarouselRenderer renderer;

    private float scrollOffset   = 0f;
    private float scrollVelocity = 0f;
    private boolean isDragging   = false;

    private static final float FRICTION     = 0.85f;
    private static final float SNAP_SPEED   = 0.15f;
    private static final int   CARD_SPACING = CarouselRenderer.getCardSpacing();

    public CarouselScreen(Screen parent) {
        super(Text.translatable("mandatory.menu.title"));
        this.parent   = parent;
        this.modules  = ModuleRegistry.getInstance().getAll();
        this.renderer = new CarouselRenderer();
    }

    @Override
    public void tick() {
        scrollVelocity *= FRICTION;
        scrollOffset   += scrollVelocity;

        float maxOffset = Math.max(0, (modules.size() - 1) * CARD_SPACING);
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

        renderer.render(context, modules, scrollOffset, width, height, delta);
        renderer.renderToggleButton(context, getActiveModule(), width, height);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean releaseOnly) {
        if (click.button() == 0 && !releaseOnly) {
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

    private Module getActiveModule() {
        if (modules.isEmpty()) return null;
        int index = Math.round(scrollOffset / CARD_SPACING);
        index = Math.max(0, Math.min(modules.size() - 1, index));
        return modules.get(index);
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
