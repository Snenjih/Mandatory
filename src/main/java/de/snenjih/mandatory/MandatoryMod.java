package de.snenjih.mandatory;

import de.snenjih.mandatory.chat.ChatCommandDispatcher;
import de.snenjih.mandatory.config.ModConfig;
import de.snenjih.mandatory.hud.NotificationManager;
import de.snenjih.mandatory.input.KeybindManager;
import de.snenjih.mandatory.menu.ModuleRegistry;
import de.snenjih.mandatory.modules.api.Module;
import de.snenjih.mandatory.modules.impl.AutoEatModule;
import de.snenjih.mandatory.modules.impl.AutoTotemModule;
import de.snenjih.mandatory.modules.impl.ElytraSwapModule;
import de.snenjih.mandatory.modules.impl.SmartReplaceModule;
import de.snenjih.mandatory.modules.impl.StackRefillModule;
import de.snenjih.mandatory.modules.impl.ToolSelectorModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MandatoryMod implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Mandatory");

    @Override
    public void onInitializeClient() {
        ModConfig config = new ModConfig();
        config.load();

        ModuleRegistry registry = ModuleRegistry.create(config);
        registry.register(new ElytraSwapModule());
        registry.register(new AutoTotemModule());
        registry.register(new StackRefillModule());
        registry.register(new AutoEatModule());
        registry.register(new ToolSelectorModule());
        registry.register(new SmartReplaceModule());

        registerEvents(registry);
    }

    private static void registerEvents(ModuleRegistry registry) {
        // ---- Tick -------------------------------------------------------
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            KeybindManager.onTick();
            NotificationManager.tick();
            for (Module m : registry.getAll()) {
                if (m.isEnabled()) {
                    try { m.onClientTick(client); }
                    catch (Exception e) { LOGGER.error("[Mandatory] {} crashed in onClientTick", m.getId(), e); }
                }
            }
        });

        // ---- HUD render -------------------------------------------------
        HudElementRegistry.addLast(
                Identifier.of("mandatory", "hud"),
                (ctx, tickCounter) -> {
                    float delta = tickCounter.getTickProgress(false);
                    for (Module m : registry.getAll()) {
                        if (m.isEnabled()) {
                            try { m.onRenderHud(ctx, delta); }
                            catch (Exception e) { LOGGER.error("[Mandatory] {} crashed in onRenderHud", m.getId(), e); }
                        }
                    }
                    net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
                    NotificationManager.render(ctx,
                            mc.getWindow().getScaledWidth(),
                            mc.getWindow().getScaledHeight());
                });

        // ---- World render -----------------------------------------------
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            for (Module m : registry.getAll()) {
                if (m.isEnabled()) {
                    try { m.onRenderWorld(ctx); }
                    catch (Exception e) { LOGGER.error("[Mandatory] {} crashed in onRenderWorld", m.getId(), e); }
                }
            }
        });

        // ---- World join / leave ----------------------------------------
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.world == null) return;
            for (Module m : registry.getAll()) {
                try { m.onJoinWorld(client.world); }
                catch (Exception e) { LOGGER.error("[Mandatory] {} crashed in onJoinWorld", m.getId(), e); }
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            for (Module m : registry.getAll()) {
                try { m.onLeaveWorld(); }
                catch (Exception e) { LOGGER.error("[Mandatory] {} crashed in onLeaveWorld", m.getId(), e); }
            }
        });

        // ---- Outgoing chat (also handles client commands) ---------------
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            // Chat commands are consumed before server sees them
            if (ChatCommandDispatcher.handle(message)) return false;
            // Dispatch to modules that want to intercept chat
            for (Module m : registry.getAll()) {
                if (!m.isEnabled()) continue;
                try {
                    ActionResult result = m.onSendChat(message);
                    if (result != ActionResult.PASS) return false;
                } catch (Exception e) { LOGGER.error("[Mandatory] {} crashed in onSendChat", m.getId(), e); }
            }
            return true;
        });

        // ---- Attack entity ---------------------------------------------
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(player instanceof ClientPlayerEntity cp)) return ActionResult.PASS;
            for (Module m : registry.getAll()) {
                if (!m.isEnabled()) continue;
                try {
                    ActionResult result = m.onAttackEntity(cp, entity);
                    if (result != ActionResult.PASS) return result;
                } catch (Exception e) { LOGGER.error("[Mandatory] {} crashed in onAttackEntity", m.getId(), e); }
            }
            return ActionResult.PASS;
        });
    }
}
