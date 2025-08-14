package de.catstorm.trilife.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageTracker.class)
public abstract class DamageTrackerMixin {
    @Shadow @Final private LivingEntity entity;

    @Inject(method = "onDamage", at = @At("HEAD"))
    private void onDamage(DamageSource damageSource, float damage, CallbackInfo ci) {
        if (damageSource.getAttacker() instanceof LivingEntity attacker && entity instanceof PlayerEntity) {
            attacker.removeStatusEffect(StatusEffects.INVISIBILITY);
        }
    }
}