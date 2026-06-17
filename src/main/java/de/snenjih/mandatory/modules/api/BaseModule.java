package de.snenjih.mandatory.modules.api;

import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseModule implements Module {

    private final String         id;
    private final String         name;
    private final String         description;
    private final ModuleCategory category;
    private final Identifier     icon;
    private       boolean        enabled  = false;
    private final List<ModuleSetting<?>> settings = new ArrayList<>();

    protected BaseModule(String id, String name, String description,
                         ModuleCategory category, Identifier icon) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.category    = category;
        this.icon        = icon;
    }

    @Override public final String         getId()          { return id; }
    @Override public final String         getName()        { return name; }
    @Override public final String         getDescription() { return description; }
    @Override public final ModuleCategory getCategory()    { return category; }
    @Override public final Identifier     getIconTexture() { return icon; }
    @Override public final boolean        isEnabled()      { return enabled; }

    @Override
    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    protected <T> ModuleSetting<T> addSetting(ModuleSetting<T> setting) {
        settings.add(setting);
        return setting;
    }

    public List<ModuleSetting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }
}
