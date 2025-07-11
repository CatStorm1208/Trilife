package de.catstorm.trilife.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @SuppressWarnings("unused")
    @Shadow protected abstract void renderFloatingItem(DrawContext context, float tickDelta);

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void showFloatingItem(ItemStack floatingItem, CallbackInfo ci) {
        if (floatingItem.isOf(Items.TOTEM_OF_UNDYING)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFloatingItem", at = @At("HEAD"))
    private void renderFloatingItem(DrawContext context, float tickDelta, CallbackInfo ci) {
    }
}