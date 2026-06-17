package de.snenjih.mandatory.menu;

import de.snenjih.mandatory.config.ModConfig;
import de.snenjih.mandatory.modules.api.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ModuleRegistry {

    private static ModuleRegistry instance;

    private final List<Module> modules = new ArrayList<>();
    private final ModConfig config;

    private ModuleRegistry(ModConfig config) {
        this.config = config;
    }

    public static ModuleRegistry create(ModConfig config) {
        instance = new ModuleRegistry(config);
        return instance;
    }

    public static ModuleRegistry getInstance() {
        if (instance == null) throw new IllegalStateException("ModuleRegistry not yet initialised");
        return instance;
    }

    public void register(Module module) {
        boolean saved = config.isEnabled(module.getId(), module.isEnabled());
        module.setEnabled(saved);
        if (saved) module.onEnable();
        modules.add(module);
    }

    public void toggle(Module module) {
        boolean next = !module.isEnabled();
        module.setEnabled(next);
        if (next) module.onEnable(); else module.onDisable();
        config.setEnabled(module.getId(), next);
    }

    public List<Module> getAll() {
        return Collections.unmodifiableList(modules);
    }

    public Optional<Module> getById(String id) {
        return modules.stream().filter(m -> m.getId().equals(id)).findFirst();
    }
}
