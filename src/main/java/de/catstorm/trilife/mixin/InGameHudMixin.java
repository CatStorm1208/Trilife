package de.catstorm.trilife.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique private InGameHud THIS = (InGameHud) (Object) this;
    @Shadow @Final private MinecraftClient client;
    @Shadow private int heldItemTooltipFade;
    @Shadow private ItemStack currentStack;

    @Inject(method = "renderHeldItemTooltip", cancellable = true, at = @At(value = "HEAD"))
    private void renderHeldItemTooltip(DrawContext context, CallbackInfo ci) {
        //Damn I love stealing code from Mojang :3
        client.getProfiler().push("selectedItemName");
        if (heldItemTooltipFade > 0 && !currentStack.isEmpty()) {
            int l;
            MutableText mutableText = Text.empty().append(currentStack.getName()).formatted(currentStack.getRarity().getFormatting());
            if (currentStack.contains(DataComponentTypes.CUSTOM_NAME)) {
                mutableText.formatted(Formatting.ITALIC);
            }
            int i = THIS.getTextRenderer().getWidth(mutableText);
            int j = (context.getScaledWindowWidth() - i) / 2;
            int k = context.getScaledWindowHeight() - 69; //nice
            assert client.interactionManager != null;
            if (!client.interactionManager.hasStatusBars()) {
                k += 24;
            }
            if ((l = (int)((float)heldItemTooltipFade * 256.0f / 10.0f)) > 255) {
                l = 255;
            }
            if (l > 0) {
                context.drawTextWithBackground(THIS.getTextRenderer(), mutableText, j, k, i, ColorHelper.Argb.withAlpha(l, -1));
            }
        }
        client.getProfiler().pop();
        ci.cancel();
    }
}