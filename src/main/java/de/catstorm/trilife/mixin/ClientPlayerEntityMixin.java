package de.catstorm.trilife.mixin;

import de.catstorm.trilife.client.TrilifeClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "requestRespawn", at = @At("HEAD"))
    private void requestRespawn(CallbackInfo ci) {
        TrilifeClient.animationCanStart = true;
    }
}