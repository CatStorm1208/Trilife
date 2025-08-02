package de.catstorm.trilife.mixin;

import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static de.catstorm.trilife.Trilife.LOGGER;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Inject(method = "onClientStatus", at = @At("HEAD"))
    private void onClientStatusHead(ClientStatusC2SPacket packet, CallbackInfo ci) {
    }
}