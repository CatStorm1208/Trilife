package de.catstorm.trilife.mixin;

import de.catstorm.trilife.item.TrilifeItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow @Final public PlayerEntity player;
    @Shadow @Final private List<DefaultedList<ItemStack>> combinedInventory;

    @Inject(method = "dropAll", at = @At("HEAD"), cancellable = true)
    private void dropAll(CallbackInfo ci) {
        for (var item : player.getHandItems()) if (item.isOf(TrilifeItems.LOOT_TOTEM)) {
            ci.cancel();
            return;
        }
        for (var list : combinedInventory) for (int i = 0; i < list.size(); i++) {
            ItemStack itemStack = list.get(i);
            if (!itemStack.isEmpty() && Math.random() > 0.33) {
                player.dropItem(itemStack, true, false);
                list.set(i, ItemStack.EMPTY);
            }
        }
        ci.cancel();
    }
}