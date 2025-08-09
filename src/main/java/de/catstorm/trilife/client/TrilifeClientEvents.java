package de.catstorm.trilife.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TrilifeClientEvents {
    private static final Identifier EMPTY_HEART = Identifier.ofVanilla("hud/heart/container");
    private static final Identifier FILLED_HEART = Identifier.of("minecraft", "hud/heart/frozen_full"); //TODO: replace this
    private static final Identifier FOURTH_HEART = Identifier.of("minecraft", "hud/heart/poisoned_full"); //TODO: and this

    public static void initClientEvents() {
        HudRenderCallback.EVENT.register(TrilifeClientEvents::render);
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        if (player.isCreative()) return;

        int x = (context.getScaledWindowWidth() / 2) - 5;
        int y = context.getScaledWindowHeight() - 45;
        int offset = 12;

        RenderSystem.enableBlend();
        if (TrilifeClient.lives < 4) {
            context.drawGuiTexture(EMPTY_HEART, x, y-offset, 9, 9);
            context.drawGuiTexture(EMPTY_HEART, x+(offset/2), y, 9, 9);
            context.drawGuiTexture(EMPTY_HEART, x-(offset/2), y, 9, 9);

            switch (TrilifeClient.lives) {
                case 3: context.drawGuiTexture(FILLED_HEART, x, y-offset, 9, 9);
                case 2: context.drawGuiTexture(FILLED_HEART, x+(offset/2), y, 9, 9);
                case 1: context.drawGuiTexture(FILLED_HEART, x-(offset/2), y, 9, 9);
            }
        }
        else {
            context.drawGuiTexture(EMPTY_HEART, x-2, y-(offset/2), 12, 12);
            context.drawGuiTexture(FOURTH_HEART, x-2, y-(offset/2), 12, 12);
        }
        RenderSystem.disableBlend();
    }
}