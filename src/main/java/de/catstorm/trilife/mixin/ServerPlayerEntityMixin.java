package de.catstorm.trilife.mixin;

import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Unique private ServerPlayerEntity THIS = (ServerPlayerEntity) (Object) this;
    @Shadow @Final public MinecraftServer server;

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        server.execute(() -> ServerPlayNetworking.send(THIS, new TotemFloatPayload(3)));
    }

    //fucking hell finally
    @Inject(method = "copyFrom", at = @At("HEAD"))
    private void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        THIS.getInventory().clone(oldPlayer.getInventory());
        if (!alive) for (var item : oldPlayer.getHandItems()) if (item.isOf(TrilifeItems.LOOT_TOTEM)) {
            item.decrement(1);
            THIS.experienceLevel = oldPlayer.experienceLevel;
            THIS.totalExperience = oldPlayer.totalExperience;
            THIS.experienceProgress = oldPlayer.experienceProgress;
            THIS.setScore(oldPlayer.getScore());
            break;
        }
    }
}