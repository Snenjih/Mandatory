package de.snenjih.mandatory.chat;

import com.google.gson.JsonPrimitive;
import de.snenjih.mandatory.config.ModConfig;
import de.snenjih.mandatory.hud.NotificationManager;
import de.snenjih.mandatory.hud.NotificationManager.Type;
import de.snenjih.mandatory.menu.ModuleRegistry;
import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.Module;
import de.snenjih.mandatory.modules.api.settings.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public final class ChatCommandDispatcher {

    private static final String PREFIX = ".";

    private ChatCommandDispatcher() {}

    /**
     * Handle a chat message. Returns true if the message was a client command
     * and should not be sent to the server.
     */
    public static boolean handle(String message) {
        if (!message.startsWith(PREFIX)) return false;
        String body = message.substring(PREFIX.length()).stripLeading();
        if (body.isEmpty()) return false;

        String[] parts = body.split(" ", 3);
        switch (parts[0]) {
            case "toggle"  -> handleToggle(parts);
            case "modules" -> handleModules();
            case "set"     -> handleSet(parts);
            case "help"    -> handleHelp();
            default        -> NotificationManager.push("Unknown command: " + parts[0], Type.ERROR);
        }
        return true;
    }

    private static void handleToggle(String[] parts) {
        if (parts.length < 2 || parts[1].isBlank()) {
            NotificationManager.push("Usage: .toggle <module-id>", Type.ERROR);
            return;
        }
        Optional<Module> opt = ModuleRegistry.getInstance().getById(parts[1]);
        if (opt.isEmpty()) {
            NotificationManager.push("Module not found: " + parts[1], Type.ERROR);
            return;
        }
        Module m = opt.get();
        ModuleRegistry.getInstance().toggle(m);
        NotificationManager.push(
                m.getName() + (m.isEnabled() ? " enabled" : " disabled"),
                m.isEnabled() ? Type.SUCCESS : Type.INFO
        );
    }

    private static void handleModules() {
        var chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
        List<Module> modules = ModuleRegistry.getInstance().getAll();
        if (modules.isEmpty()) {
            chatHud.addMessage(Text.literal("§7[Mandatory] No modules registered."));
            return;
        }
        chatHud.addMessage(Text.literal("§6[Mandatory] Modules:"));
        for (Module m : modules) {
            String status = m.isEnabled() ? "§a[ON]§r" : "§c[OFF]§r";
            chatHud.addMessage(Text.literal("  " + status + " §f" + m.getName()
                    + " §8(" + m.getId() + ")"));
        }
    }

    private static void handleSet(String[] parts) {
        if (parts.length < 3) {
            NotificationManager.push("Usage: .set <id>.<setting> <value>", Type.ERROR);
            return;
        }
        String[] target = parts[1].split("\\.", 2);
        if (target.length < 2) {
            NotificationManager.push("Usage: .set <id>.<setting> <value>", Type.ERROR);
            return;
        }
        Optional<Module> opt = ModuleRegistry.getInstance().getById(target[0]);
        if (opt.isEmpty() || !(opt.get() instanceof BaseModule bm)) {
            NotificationManager.push("Module not found: " + target[0], Type.ERROR);
            return;
        }
        Optional<ModuleSetting<?>> settingOpt = bm.getSettings().stream()
                .filter(s -> s.getId().equals(target[1]))
                .findFirst();
        if (settingOpt.isEmpty()) {
            NotificationManager.push("Setting not found: " + target[1], Type.ERROR);
            return;
        }
        try {
            applyAndSave(bm, settingOpt.get(), parts[2].strip());
            NotificationManager.push(target[0] + "." + target[1] + " = " + parts[2].strip(),
                    Type.SUCCESS);
        } catch (Exception e) {
            NotificationManager.push("Invalid value: " + parts[2].strip(), Type.ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void applyAndSave(BaseModule module, ModuleSetting<T> setting, String raw) {
        T parsed = setting.fromJson(new JsonPrimitive(raw));
        setting.set(parsed);
        ModConfig.getInstance().saveModuleSettings(module);
    }

    private static void handleHelp() {
        var chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
        chatHud.addMessage(Text.literal("§6[Mandatory] Commands:"));
        chatHud.addMessage(Text.literal("  §f.toggle <id>          §8toggle a module"));
        chatHud.addMessage(Text.literal("  §f.modules              §8list all modules"));
        chatHud.addMessage(Text.literal("  §f.set <id>.<key> <val> §8change a setting"));
        chatHud.addMessage(Text.literal("  §f.help                 §8show this help"));
    }
}
