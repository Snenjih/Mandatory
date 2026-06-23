package de.snenjih.mandatory.modules.impl.sprint_toggle;

import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import de.snenjih.mandatory.modules.api.settings.BooleanSetting;
import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;

public class SprintToggleModule extends BaseModule {

    private final ModuleSetting<Boolean> sprintInWater;
    private final ModuleSetting<Boolean> sprintInAir;

    public SprintToggleModule() {
        super(
            "sprint_toggle",
            "Sprint Toggle",
            "Automatically sprints without holding the sprint key.",
            ModuleCategory.UTILITY,
            Identifier.of("mandatory", "modules/sprint_toggle")
        );
        sprintInWater = addSetting(new BooleanSetting("sprint_in_water", "Sprint in Water", false));
        sprintInAir   = addSetting(new BooleanSetting("sprint_in_air",   "Sprint in Air",   true));
    }

    @Override
    public void onClientTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if (player.isSpectator()) return;

        boolean canSprint = !player.isSneaking()
                && player.getHungerManager().getFoodLevel() > 6
                && !player.isUsingItem()
                && !player.hasStatusEffect(StatusEffects.BLINDNESS);

        if (!sprintInWater.get() && player.isTouchingWater()) canSprint = false;
        if (!sprintInAir.get() && !player.isOnGround() && !player.isGliding()) canSprint = false;

        if (canSprint && player.forwardSpeed > 0.0f) {
            player.setSprinting(true);
        }
    }
}
