package de.catstorm.trilife.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import static de.catstorm.trilife.Trilife.MOD_ID;
import de.catstorm.trilife.client.TrilifeClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {
    @Unique private static long deathAnimationStartTime = 0;
    @Unique private static final int deathAnimationFrameCount = 21;
    @Unique private static final int deathAnimationFps = 20;
    @Unique private static final ArrayList<Identifier> DEATH_TEXTURES = new ArrayList<>();

    @Inject(method = "init", at = @At("HEAD"))
    private void initHead(CallbackInfo ci) {
        for (int i = 0; i < deathAnimationFrameCount; i++) {
            DEATH_TEXTURES.add(Identifier.of(MOD_ID, "hud/death/animation" + i));
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        RenderSystem.enableBlend();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;

        if (player.isDead() && TrilifeClient.animationCanStart) {
            deathAnimationStartTime = System.currentTimeMillis();
            TrilifeClient.animationCanStart = false;
        }

        int frame = Math.round((System.currentTimeMillis() - deathAnimationStartTime) / (1/(((float) deathAnimationFps)/1000)));
        if (frame < deathAnimationFrameCount) {
            context.getMatrices().push();
            context.getMatrices().scale(2.0f, 2.0f, 2.0f);
            context.getMatrices().pop();

            context.drawGuiTexture(DEATH_TEXTURES.get(frame), (context.getScaledWindowWidth()/2)-5,
                (context.getScaledWindowHeight()/2)-65, 40, 40);
            RenderSystem.disableBlend();
            ci.cancel();
        }
        RenderSystem.disableBlend();
    }
}