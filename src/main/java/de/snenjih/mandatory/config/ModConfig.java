package de.snenjih.mandatory.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("mandatory.json");
    private static final Type MAP_TYPE = new TypeToken<Map<String, Boolean>>() {}.getType();

    private final Map<String, Boolean> states = new HashMap<>();

    public void load() {
        if (!Files.exists(CONFIG_PATH)) return;
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Map<String, Boolean> loaded = GSON.fromJson(reader, MAP_TYPE);
            if (loaded != null) states.putAll(loaded);
        } catch (IOException e) {
            System.err.println("[Mandatory] Failed to load config: " + e.getMessage());
        }
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(states, writer);
        } catch (IOException e) {
            System.err.println("[Mandatory] Failed to save config: " + e.getMessage());
        }
    }

    public boolean isEnabled(String moduleId, boolean defaultValue) {
        return states.getOrDefault(moduleId, defaultValue);
    }

    public void setEnabled(String moduleId, boolean enabled) {
        states.put(moduleId, enabled);
        save();
    }
}
