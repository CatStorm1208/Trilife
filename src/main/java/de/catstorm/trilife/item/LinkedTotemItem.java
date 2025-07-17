package de.catstorm.trilife.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;

public class LinkedTotemItem extends TotemItem {
    public LinkedTotemItem(Settings settings, float health, StatusEffectInstance... effects) {
        super(settings, health, effects);
    }

    @Override
    public void onPop(DamageSource source, LivingEntity owner) {
        super.onPop(source, owner);
    }
}