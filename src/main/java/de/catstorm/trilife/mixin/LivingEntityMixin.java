package de.catstorm.trilife.mixin;

import de.catstorm.trilife.item.HeartTotemItem;
import de.catstorm.trilife.item.TotemItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static de.catstorm.trilife.Trilife.LOGGER;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    LivingEntity THIS = (LivingEntity) (Object) this;

    @SuppressWarnings("unused")
    @Shadow protected abstract boolean tryUseTotem(DamageSource source);
    @Shadow public abstract Iterable<ItemStack> getHandItems();

    @Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
    private void tryUseTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        for (var item : getHandItems()) {
            if (item.getItem() instanceof TotemItem) {
                TotemItem totem = (TotemItem) item.getItem();
                totem.onPop(source, THIS);

                LOGGER.info(item.toString());
                if (item.getItem() instanceof HeartTotemItem) {
                    cir.setReturnValue(false);
                }
                else {
                    cir.setReturnValue(true);
                    THIS.getWorld().sendEntityStatus(THIS, (byte) 35);
                }
                item.decrement(1);
                break;
            }
        }
        cir.cancel();
    }
}