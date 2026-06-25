package de.snenjih.mandatory.modules.impl.anti_vignette;

import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import de.snenjih.mandatory.modules.api.settings.BooleanSetting;
import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.minecraft.util.Identifier;

public class AntiVignetteModule extends BaseModule {

    public static AntiVignetteModule INSTANCE = null;

    public final ModuleSetting<Boolean> disableUnderwater;
    public final ModuleSetting<Boolean> disablePumpkin;

    public AntiVignetteModule() {
        super(
            "anti_vignette",
            "Anti Vignette",
            "Disables the screen edge vignette and optional overlays.",
            ModuleCategory.VISUAL,
            Identifier.of("mandatory", "modules/anti_vignette")
        );
        disableUnderwater = addSetting(new BooleanSetting("disable_underwater", "Disable Underwater Overlay", false));
        disablePumpkin    = addSetting(new BooleanSetting("disable_pumpkin",    "Disable Pumpkin Overlay",    false));
    }

    @Override public void onEnable()  { INSTANCE = this; }
    @Override public void onDisable() { INSTANCE = null; }
}
