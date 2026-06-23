package de.snenjih.mandatory.mixin;

import de.snenjih.mandatory.menu.CarouselScreen;
import de.snenjih.mandatory.menu.ScreenshotGalleryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addMandatoryButtons(CallbackInfo ci) {
        // Screenshots button sits 44px above the vanilla button area.
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("mandatory.screenshots.open"),
            btn -> MinecraftClient.getInstance().setScreen(
                new ScreenshotGalleryScreen((Screen) (Object) this)
            )
        ).dimensions(width / 2 - 100, height / 4 - 44, 200, 20).build());

        // Mandatory carousel button sits 20px above the vanilla button area.
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Mandatory"),
            btn -> MinecraftClient.getInstance().setScreen(
                new CarouselScreen((Screen) (Object) this)
            )
        ).dimensions(width / 2 - 100, height / 4 - 20, 200, 20).build());
    }
}
