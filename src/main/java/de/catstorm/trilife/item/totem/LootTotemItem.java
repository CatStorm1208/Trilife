package de.catstorm.trilife.item.totem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;

public class LootTotemItem extends TotemItem {
    public LootTotemItem(Settings settings, float health, StatusEffectInstance... effects) {
        super(settings, health, effects);
    }

    @Override
    @Deprecated
    public void onPop(DamageSource source, LivingEntity owner) {
        //I love inheritance... until this shit happens
    }
}