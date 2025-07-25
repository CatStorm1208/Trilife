package de.catstorm.trilife.mixin;

import de.catstorm.trilife.item.TrilifeItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow public abstract Iterable<ItemStack> getHandItems();

    @Inject(method = "dropInventory", at = @At("HEAD"), cancellable = true)
    private void dropInventory(CallbackInfo ci) {
        for (var item : getHandItems()) if (item.isOf(TrilifeItems.LOOT_TOTEM)) {
            ci.cancel();
        }
    }
}