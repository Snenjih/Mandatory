package de.snenjih.mandatory.modules.impl.food_tooltip;

import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import de.snenjih.mandatory.modules.api.settings.BooleanSetting;
import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.minecraft.util.Identifier;

public class FoodTooltipModule extends BaseModule {

    public static FoodTooltipModule INSTANCE;

    public final ModuleSetting<Boolean> showSaturation;
    public final ModuleSetting<Boolean> showEffects;
    public final ModuleSetting<Boolean> showEffectiveSaturation;

    public FoodTooltipModule() {
        super(
            "food_tooltip",
            "Food Tooltip",
            "Shows hunger, saturation, and effects in food item tooltips.",
            ModuleCategory.UTILITY,
            Identifier.of("mandatory", "modules/food_tooltip")
        );
        INSTANCE = this;
        showSaturation          = addSetting(new BooleanSetting("show_saturation",           "Show Saturation",           true));
        showEffects             = addSetting(new BooleanSetting("show_effects",               "Show Effects",              true));
        showEffectiveSaturation = addSetting(new BooleanSetting("show_effective_saturation", "Show Effective Saturation", false));
    }
}
