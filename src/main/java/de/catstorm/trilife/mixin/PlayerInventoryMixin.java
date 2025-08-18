package de.catstorm.trilife.mixin;

import de.catstorm.trilife.item.TrilifeItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow @Final public PlayerEntity player;

    @Inject(method = "dropAll", at = @At("HEAD"), cancellable = true)
    private void dropAll(CallbackInfo ci) {
        for (var item : player.getHandItems()) if (item.isOf(TrilifeItems.LOOT_TOTEM)) ci.cancel();
    }
}