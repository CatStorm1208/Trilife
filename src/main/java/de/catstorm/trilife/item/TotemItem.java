package de.catstorm.trilife.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import java.util.Set;

public class TotemItem extends Item {
    public Set<StatusEffectInstance> effects;
    public float health;

    public TotemItem(Settings settings, float health, StatusEffectInstance... effects) {
        super(settings);
        this.effects = Set.of(effects);
        this.health = health;
    }

    public void onPop(DamageSource source, LivingEntity owner) {
        owner.setHealth(health);
        owner.clearStatusEffects();
        for (var effect : effects) {
            owner.addStatusEffect(effect);
        }
    }
}