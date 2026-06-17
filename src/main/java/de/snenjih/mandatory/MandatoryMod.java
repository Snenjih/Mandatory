package de.snenjih.mandatory;

import de.snenjih.mandatory.config.ModConfig;
import de.snenjih.mandatory.menu.ModuleRegistry;
import de.snenjih.mandatory.modules.impl.ElytraSwapModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MandatoryMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModConfig config = new ModConfig();
        config.load();

        ModuleRegistry registry = ModuleRegistry.create(config);
        registry.register(new ElytraSwapModule());
    }
}
