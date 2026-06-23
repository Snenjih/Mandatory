package de.snenjih.mandatory.modules.impl.inventory_lock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import de.snenjih.mandatory.modules.api.settings.BooleanSetting;
import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class InventoryLockModule extends BaseModule {

    public static InventoryLockModule INSTANCE;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public final ModuleSetting<Boolean> showLockIcon;

    private final Set<Integer> lockedSlots = new HashSet<>();

    public InventoryLockModule() {
        super(
            "inventory_lock",
            "Inventory Lock",
            "Locks inventory slots to prevent accidental item movement or dropping.",
            ModuleCategory.UTILITY,
            Identifier.of("mandatory", "modules/inventory_lock")
        );
        INSTANCE = this;
        showLockIcon = addSetting(new BooleanSetting("show_lock_icon", "Show Lock Icon", true));
    }

    @Override
    public void onEnable() {
        loadLockedSlots();
    }

    @Override
    public void onDisable() {
        saveLockedSlots();
    }

    public boolean isSlotLocked(int slotId) {
        return isEnabled() && lockedSlots.contains(slotId);
    }

    public void toggleSlot(int slotId) {
        if (lockedSlots.contains(slotId)) {
            lockedSlots.remove(slotId);
        } else {
            lockedSlots.add(slotId);
        }
        saveLockedSlots();
    }

    private Path savePath() {
        return FabricLoader.getInstance().getConfigDir().resolve("mandatory_locks.json");
    }

    private void saveLockedSlots() {
        try {
            Files.writeString(savePath(), GSON.toJson(lockedSlots));
        } catch (IOException e) {
            System.err.println("[Mandatory] Failed to save locked slots: " + e.getMessage());
        }
    }

    private void loadLockedSlots() {
        Path path = savePath();
        if (!Files.exists(path)) return;
        try {
            String json = Files.readString(path);
            Type setType = new TypeToken<Set<Integer>>() {}.getType();
            Set<Integer> loaded = GSON.fromJson(json, setType);
            lockedSlots.clear();
            if (loaded != null) lockedSlots.addAll(loaded);
        } catch (Exception e) {
            System.err.println("[Mandatory] Failed to load locked slots: " + e.getMessage());
        }
    }
}
