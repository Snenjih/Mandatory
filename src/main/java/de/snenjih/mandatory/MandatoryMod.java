package de.snenjih.mandatory;

import de.snenjih.mandatory.chat.ChatCommandDispatcher;
import de.snenjih.mandatory.config.ModConfig;
import de.snenjih.mandatory.hud.NotificationManager;
import de.snenjih.mandatory.input.KeybindManager;
import de.snenjih.mandatory.menu.MainMenuScreen;
import de.snenjih.mandatory.menu.ModuleRegistry;
import de.snenjih.mandatory.modules.api.HudElement;
import de.snenjih.mandatory.modules.api.HudRegistry;
import de.snenjih.mandatory.modules.api.Module;
import de.snenjih.mandatory.modules.impl.anti_afk.AntiAfkModule;
import de.snenjih.mandatory.modules.impl.armor_status_hud.ArmorStatusHudModule;
import de.snenjih.mandatory.modules.impl.auto_eat.AutoEatModule;
import de.snenjih.mandatory.modules.impl.auto_totem.AutoTotemModule;
import de.snenjih.mandatory.modules.impl.coordinates_hud.CoordinatesHudModule;
import de.snenjih.mandatory.modules.impl.death_coordinates.DeathCoordinatesModule;
import de.snenjih.mandatory.modules.impl.elytra_swap.ElytraSwapModule;
import de.snenjih.mandatory.modules.impl.food_tooltip.FoodTooltipModule;
import de.snenjih.mandatory.modules.impl.fps_ping_hud.FpsPingHudModule;
import de.snenjih.mandatory.modules.impl.inventory_lock.InventoryLockModule;
import de.snenjih.mandatory.modules.impl.keystrokes_hud.KeystrokesHudModule;
import de.snenjih.mandatory.modules.impl.middle_click_pick.MiddleClickPickModule;
import de.snenjih.mandatory.modules.impl.potion_effects_hud.PotionEffectsHudModule;
import de.snenjih.mandatory.modules.impl.smart_replace.SmartReplaceModule;
import de.snenjih.mandatory.modules.impl.biome_display.BiomeDisplayModule;
import de.snenjih.mandatory.modules.impl.crosshair_customizer.CrosshairCustomizerModule;
import de.snenjih.mandatory.modules.impl.durability_hud.DurabilityHudModule;
import de.snenjih.mandatory.modules.impl.fullbright.FullbrightModule;
import de.snenjih.mandatory.modules.impl.hit_color.HitColorModule;
import de.snenjih.mandatory.modules.impl.item_age_timer.ItemAgeTimerModule;
import de.snenjih.mandatory.modules.impl.mc_time_display.McTimeDisplayModule;
import de.snenjih.mandatory.modules.impl.rain_disable.RainDisableModule;
import de.snenjih.mandatory.modules.impl.real_time_clock.RealTimeClockModule;
import de.snenjih.mandatory.modules.impl.saturation_bar.SaturationBarModule;
import de.snenjih.mandatory.modules.impl.scoreboard_hud.ScoreboardHudModule;
import de.snenjih.mandatory.modules.impl.speed_display.SpeedDisplayModule;
import de.snenjih.mandatory.modules.impl.zoom.ZoomModule;
import de.snenjih.mandatory.modules.impl.sneak_toggle.SneakToggleModule;
import de.snenjih.mandatory.modules.impl.sprint_toggle.SprintToggleModule;
import de.snenjih.mandatory.modules.impl.stack_refill.StackRefillModule;
import de.snenjih.mandatory.modules.impl.nametag_badge.NametageModule;
import de.snenjih.mandatory.modules.impl.target_hp.TargetHpModule;
import de.snenjih.mandatory.modules.impl.mod_settings.ModSettingsModule;
import de.snenjih.mandatory.modules.impl.tool_selector.ToolSelectorModule;
import de.snenjih.mandatory.modules.impl.anti_fog.AntiFogModule;
import de.snenjih.mandatory.modules.impl.anti_vignette.AntiVignetteModule;
import de.snenjih.mandatory.modules.impl.item_info_hud.ItemInfoHudModule;
import de.snenjih.mandatory.modules.impl.boss_bar_customizer.BossBarCustomizerModule;
import de.snenjih.mandatory.modules.impl.damage_indicator.DamageIndicatorModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
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
        registry.register(new ModSettingsModule());
        registry.register(new ElytraSwapModule());
        registry.register(new AutoTotemModule());
        registry.register(new StackRefillModule());
        registry.register(new AutoEatModule());
        registry.register(new ToolSelectorModule());
        registry.register(new SmartReplaceModule());
        registry.register(new AntiAfkModule());
        registry.register(new MiddleClickPickModule());

        // Death Coordinates — HUD element
        DeathCoordinatesModule dcm = new DeathCoordinatesModule();
        registry.register(dcm);
        HudRegistry.register(dcm, 4, 4);

        registry.register(new FoodTooltipModule());
        registry.register(new InventoryLockModule());
        registry.register(new SprintToggleModule());
        registry.register(new SneakToggleModule());
        registry.register(new FullbrightModule());
        registry.register(new NametageModule());

        // New HUD modules
        CoordinatesHudModule coordinatesHud = new CoordinatesHudModule();
        registry.register(coordinatesHud);
        HudRegistry.register(coordinatesHud, 4, 34);

        FpsPingHudModule fpsPingHud = new FpsPingHudModule();
        registry.register(fpsPingHud);
        HudRegistry.register(fpsPingHud, 4, 50);

        ArmorStatusHudModule armorHud = new ArmorStatusHudModule();
        registry.register(armorHud);
        HudRegistry.register(armorHud, 4, 74);

        PotionEffectsHudModule potionHud = new PotionEffectsHudModule();
        registry.register(potionHud);
        HudRegistry.register(potionHud, 4, 150);

        KeystrokesHudModule keystrokesHud = new KeystrokesHudModule();
        registry.register(keystrokesHud);
        HudRegistry.register(keystrokesHud, 300, 150);

        DurabilityHudModule durabilityHud = new DurabilityHudModule();
        registry.register(durabilityHud);
        HudRegistry.register(durabilityHud, 4, 200);

        TargetHpModule targetHp = new TargetHpModule();
        registry.register(targetHp);
        HudRegistry.register(targetHp, 4, 240);

        BiomeDisplayModule biomeDisplay = new BiomeDisplayModule();
        registry.register(biomeDisplay);
        HudRegistry.register(biomeDisplay, 4, 16);

        SpeedDisplayModule speedDisplay = new SpeedDisplayModule();
        registry.register(speedDisplay);
        HudRegistry.register(speedDisplay, 4, 270);

        RealTimeClockModule realTimeClock = new RealTimeClockModule();
        registry.register(realTimeClock);
        HudRegistry.register(realTimeClock, 4, 300);

        SaturationBarModule saturationBar = new SaturationBarModule();
        registry.register(saturationBar);
        HudRegistry.register(saturationBar, 4, 330);

        registry.register(new ZoomModule());
        registry.register(new CrosshairCustomizerModule());
        registry.register(new HitColorModule());
        registry.register(new ItemAgeTimerModule());
        registry.register(new RainDisableModule());

        ScoreboardHudModule scoreboardHud = new ScoreboardHudModule();
        registry.register(scoreboardHud);
        HudRegistry.register(scoreboardHud, 4, 360);

        McTimeDisplayModule mcTimeDisplay = new McTimeDisplayModule();
        registry.register(mcTimeDisplay);
        HudRegistry.register(mcTimeDisplay, 4, 530);

        registry.register(new AntiFogModule());
        registry.register(new AntiVignetteModule());

        ItemInfoHudModule itemInfoHud = new ItemInfoHudModule();
        registry.register(itemInfoHud);
        HudRegistry.register(itemInfoHud, 4, 570);

        registry.register(new BossBarCustomizerModule());

        registry.register(new DamageIndicatorModule());

        // Right-Shift opens the Mandatory menu from in-game
        KeyBinding openMenuKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.mandatory.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeybindManager.CATEGORY
            )
        );

        registerEvents(registry, openMenuKey);
    }

    private static void registerEvents(ModuleRegistry registry, KeyBinding openMenuKey) {
        // ---- Tick -------------------------------------------------------
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            KeybindManager.onTick();
            NotificationManager.tick();
            // Right-Shift opens the carousel screen from in-game
            if (openMenuKey.wasPressed() && client.currentScreen == null) {
                client.setScreen(new MainMenuScreen(null));
            }
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
                    // Render all registered HUD elements
                    for (HudRegistry.HudEntry entry : HudRegistry.getAll()) {
                        HudElement elem = entry.element();
                        // Only render if it's a Module and that Module is enabled
                        if (elem instanceof Module m && !m.isEnabled()) continue;
                        ModConfig.HudElementState state = ModConfig.getInstance().getHudState(elem.getHudId());
                        if (state == null || !state.visible()) continue;
                        try {
                            elem.renderHud(ctx, delta, state.x(), state.y(), state.w(), state.h());
                        } catch (Exception e) {
                            LOGGER.error("[Mandatory] {} crashed in HUD render", elem.getHudId(), e);
                        }
                    }
                    MinecraftClient mc = MinecraftClient.getInstance();
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
