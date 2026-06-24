package de.snenjih.mandatory.modules.impl.fullbright;

import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import de.snenjih.mandatory.modules.api.settings.FloatSetting;
import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class FullbrightModule extends BaseModule {

    private final ModuleSetting<Float> gammaValue;

    private double savedGamma = 1.0;

    public FullbrightModule() {
        super(
            "fullbright",
            "Fullbright",
            "Overrides gamma for maximum visibility in dark areas.",
            ModuleCategory.VISUAL,
            Identifier.of("mandatory", "modules/fullbright")
        );
        gammaValue = addSetting(new FloatSetting("gamma_value", "Gamma Value", 16.0f, 1.0f, 16.0f));
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) return;
        savedGamma = mc.options.getGamma().getValue();
        mc.options.getGamma().setValue((double) gammaValue.get());
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) return;
        mc.options.getGamma().setValue(savedGamma);
    }

    @Override
    public void onClientTick(MinecraftClient client) {
        if (client.options == null) return;
        double current = client.options.getGamma().getValue();
        double target  = gammaValue.get().doubleValue();
        if (Math.abs(current - target) > 0.01) {
            client.options.getGamma().setValue(target);
        }
    }
}
