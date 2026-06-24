package de.snenjih.mandatory.modules.impl.rain_disable;

import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import de.snenjih.mandatory.modules.api.settings.BooleanSetting;
import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.minecraft.util.Identifier;

public class RainDisableModule extends BaseModule {

    public final ModuleSetting<Boolean> alsoDisableThunder;

    public RainDisableModule() {
        super(
            "rain_disable",
            "Rain Disable",
            "Disables rain and snow rendering client-side without affecting server weather.",
            ModuleCategory.VISUAL,
            Identifier.of("mandatory", "modules/rain_disable")
        );
        alsoDisableThunder = addSetting(new BooleanSetting("also_disable_thunder", "Disable Thunder Effect", true));
    }
}
