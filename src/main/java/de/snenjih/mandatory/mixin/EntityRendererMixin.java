package de.snenjih.mandatory.mixin;

import de.snenjih.mandatory.modules.impl.nametag_badge.NametageModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Redirect(
        method = "render",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/entity/Entity;getDisplayName()Lnet/minecraft/text/Text;")
    )
    private Text redirectGetDisplayName(T entity) {
        Text original = entity.getDisplayName();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || entity != mc.player) return original;
        if (!NametageModule.isBadgeActive()) return original;
        return Text.literal("✦ ").withColor(NametageModule.getBadgeColor()).append(original);
    }
}
