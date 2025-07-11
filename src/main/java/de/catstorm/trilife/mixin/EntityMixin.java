package de.catstorm.trilife.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;",
        at = @At("HEAD"), cancellable = true)
    private void dropLoot(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        if (stack.getItem().equals(Items.TOTEM_OF_UNDYING)) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
}