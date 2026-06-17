package de.snenjih.mandatory.config;

import com.google.gson.*;
import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ModConfig {

    private static final Gson GSON        = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("mandatory.json");
    private static final int  VERSION     = 2;

    private static ModConfig INSTANCE;

    // moduleId → JsonObject with "enabled" + optional setting keys
    private final Map<String, JsonObject> data = new HashMap<>();

    public ModConfig() {
        INSTANCE = this;
    }

    public static ModConfig getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Load / Save
    // -------------------------------------------------------------------------

    public void load() {
        if (!Files.exists(CONFIG_PATH)) return;
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) return;
            JsonObject obj = root.getAsJsonObject();

            int version = obj.has("_version") ? obj.get("_version").getAsInt() : 1;

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("_")) continue;

                if (version < 2) {
                    // v1: flat boolean → migrate to nested { "enabled": bool }
                    JsonObject migrated = new JsonObject();
                    if (entry.getValue().isJsonPrimitive()) {
                        migrated.addProperty("enabled", entry.getValue().getAsBoolean());
                    }
                    data.put(key, migrated);
                } else {
                    // v2: already nested objects
                    if (entry.getValue().isJsonObject()) {
                        data.put(key, entry.getValue().getAsJsonObject());
                    }
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("[Mandatory] Failed to load config: " + e.getMessage());
        }
    }

    public void save() {
        JsonObject root = new JsonObject();
        root.addProperty("_version", VERSION);
        for (Map.Entry<String, JsonObject> entry : data.entrySet()) {
            root.add(entry.getKey(), entry.getValue());
        }
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            System.err.println("[Mandatory] Failed to save config: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Enabled-state access (Module interface contract)
    // -------------------------------------------------------------------------

    public boolean isEnabled(String moduleId, boolean defaultValue) {
        JsonObject obj = data.get(moduleId);
        if (obj == null || !obj.has("enabled")) return defaultValue;
        return obj.get("enabled").getAsBoolean();
    }

    public void setEnabled(String moduleId, boolean enabled) {
        data.computeIfAbsent(moduleId, k -> new JsonObject()).addProperty("enabled", enabled);
        save();
    }

    // -------------------------------------------------------------------------
    // Per-module settings (BaseModule only)
    // -------------------------------------------------------------------------

    public void loadModuleSettings(BaseModule module) {
        JsonObject obj = data.get(module.getId());
        if (obj == null) return;
        for (ModuleSetting<?> setting : module.getSettings()) {
            if (!obj.has(setting.getId())) continue;
            try {
                loadSetting(setting, obj.get(setting.getId()));
            } catch (Exception e) {
                System.err.println("[Mandatory] Bad value for "
                        + module.getId() + "." + setting.getId() + ": " + e.getMessage());
            }
        }
    }

    public void saveModuleSettings(BaseModule module) {
        JsonObject obj = data.computeIfAbsent(module.getId(), k -> new JsonObject());
        for (ModuleSetting<?> setting : module.getSettings()) {
            obj.add(setting.getId(), setting.toJson());
        }
        save();
    }

    @SuppressWarnings("unchecked")
    private static <T> void loadSetting(ModuleSetting<T> setting, JsonElement el) {
        setting.set(setting.fromJson(el));
    }
}
