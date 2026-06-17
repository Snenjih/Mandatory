package de.snenjih.mandatory.modules.impl;

import de.snenjih.mandatory.modules.api.BaseModule;
import de.snenjih.mandatory.modules.api.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class ElytraSwapModule extends BaseModule {

    private static final int CHEST_SLOT = 6;

    public ElytraSwapModule() {
        super(
            "elytra_swap",
            "Elytra Swap",
            "Right-click elytra/chestplate to swap.",
            ModuleCategory.UTILITY,
            Identifier.of("mandatory", "modules/elytra_swap")
        );
    }

    @Override
    public ActionResult onInteractItem(ClientPlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen != null) return ActionResult.PASS;

        ItemStack handStack = player.getMainHandStack();
        if (!isChestEquippable(handStack)) return ActionResult.PASS;

        if (player.isCreative()) {
            player.sendMessage(Text.literal("Elytra Swap: Not supported in Creative mode."), true);
            return ActionResult.FAIL;
        }

        if (player.isGliding()) {
            player.sendMessage(Text.translatable("mandatory.elytra_swap.blocked.flying"), true);
            return ActionResult.FAIL;
        }

        ItemStack chestStack = player.getEquippedStack(EquipmentSlot.CHEST);

        if (!chestStack.isEmpty() && hasBindingCurse(chestStack)) {
            player.sendMessage(Text.translatable("mandatory.elytra_swap.blocked.binding"), true);
            return ActionResult.FAIL;
        }

        if (handStack.isDamageable() && handStack.getDamage() >= handStack.getMaxDamage()) {
            player.sendMessage(Text.translatable("mandatory.elytra_swap.blocked.broken"), true);
            return ActionResult.FAIL;
        }

        performSwap(mc, player, chestStack.isEmpty());
        return ActionResult.SUCCESS;
    }

    private void performSwap(MinecraftClient mc, ClientPlayerEntity player, boolean chestWasEmpty) {
        int syncId    = player.playerScreenHandler.syncId;
        int hotbarIdx = 36 + player.getInventory().getSelectedSlot();

        mc.interactionManager.clickSlot(syncId, hotbarIdx,  0, SlotActionType.PICKUP, player);
        mc.interactionManager.clickSlot(syncId, CHEST_SLOT, 0, SlotActionType.PICKUP, player);
        if (!chestWasEmpty) {
            mc.interactionManager.clickSlot(syncId, hotbarIdx, 0, SlotActionType.PICKUP, player);
        }
    }

    private static boolean isChestEquippable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        EquippableComponent equippable = stack.get(DataComponentTypes.EQUIPPABLE);
        return equippable != null && equippable.slot() == EquipmentSlot.CHEST;
    }

    private static boolean hasBindingCurse(ItemStack stack) {
        ItemEnchantmentsComponent enchantments = stack.getEnchantments();
        return enchantments.getEnchantments().stream()
            .anyMatch(entry -> entry.matchesKey(Enchantments.BINDING_CURSE));
    }
}
