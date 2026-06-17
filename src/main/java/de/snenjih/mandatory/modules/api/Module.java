package de.snenjih.mandatory.modules.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public interface Module {

    String getId();

    String getName();

    String getDescription();

    Identifier getIconTexture();

    ModuleCategory getCategory();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    default void onEnable() {}

    default void onDisable() {}

    default void onClientTick(MinecraftClient client) {}
}
