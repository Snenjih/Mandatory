package de.snenjih.mandatory.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public final class QuickMessageConfig {

    private static final Gson GSON  = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH  = FabricLoader.getInstance()
            .getConfigDir().resolve("mandatory_quickmsg.json");
    private static final int  SLOTS = 5;

    private final String[] messages = new String[SLOTS + 1]; // index 1..5, index 0 unused

    public QuickMessageConfig() {
        for (int i = 1; i <= SLOTS; i++) messages[i] = "";
    }

    public void load() {
        if (!Files.exists(PATH)) return;
        try (Reader r = Files.newBufferedReader(PATH)) {
            JsonObject obj = JsonParser.parseReader(r).getAsJsonObject();
            JsonObject slots = obj.getAsJsonObject("slots");
            for (int i = 1; i <= SLOTS; i++) {
                String key = String.valueOf(i);
                if (slots.has(key)) messages[i] = slots.get(key).getAsString();
            }
        } catch (Exception e) {
            System.err.println("[Mandatory] Failed to load quick-msg config: " + e.getMessage());
        }
    }

    public void save() {
        JsonObject slots = new JsonObject();
        for (int i = 1; i <= SLOTS; i++) slots.addProperty(String.valueOf(i), messages[i]);
        JsonObject root = new JsonObject();
        root.add("slots", slots);
        try (Writer w = Files.newBufferedWriter(PATH)) {
            GSON.toJson(root, w);
        } catch (IOException e) {
            System.err.println("[Mandatory] Failed to save quick-msg config: " + e.getMessage());
        }
    }

    /** 1-based slot index. Returns empty string if unset or out of range. */
    public String get(int slot) {
        if (slot < 1 || slot > SLOTS) return "";
        return messages[slot];
    }

    /** 1-based. Trims the value. Returns false if index is out of range. */
    public boolean set(int slot, String text) {
        if (slot < 1 || slot > SLOTS) return false;
        messages[slot] = text == null ? "" : text.strip();
        save();
        return true;
    }

    public int slotCount() { return SLOTS; }
}
