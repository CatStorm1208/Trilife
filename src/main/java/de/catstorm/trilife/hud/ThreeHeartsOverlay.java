package de.catstorm.trilife.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import static de.catstorm.trilife.Trilife.MOD_ID;

public class ThreeHeartsOverlay implements HudRenderCallback {
    public static final Identifier FULL_HEART = Identifier.of(MOD_ID, "textures/hud/green_heart.png");
    public static final Identifier EMPTY_HEART = Identifier.of(MOD_ID, "textures/hud/empty_heart.png");

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        int x = 0;
        int y = 0;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client != null) {
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledWidth();

            x = width /2;
            y = height;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, EMPTY_HEART);

        drawContext.drawTexture(EMPTY_HEART, x-9, y -54, 0, 0, 9, 9, 9, 9); //TODO: make shit work
    }
}