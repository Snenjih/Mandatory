package de.snenjih.mandatory.mixin;

import de.snenjih.mandatory.menu.ModuleRegistry;
import de.snenjih.mandatory.modules.impl.ElytraSwapModule;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientInteractionMixin {

    @Inject(
        method = "interactItem",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onInteractItem(PlayerEntity player, Hand hand,
                                 CallbackInfoReturnable<ActionResult> cir) {
        if (!(player instanceof ClientPlayerEntity clientPlayer)) return;

        ModuleRegistry.getInstance().getById("elytra_swap").ifPresent(module -> {
            if (module instanceof ElytraSwapModule esm) {
                ActionResult result = esm.trySwap(clientPlayer, hand);
                if (result != ActionResult.PASS) {
                    cir.setReturnValue(result);
                    cir.cancel();
                }
            }
        });
    }
}
