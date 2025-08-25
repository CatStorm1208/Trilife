package de.catstorm.trilife.item.totem;

import static de.catstorm.trilife.logic.FuckTheJavaStandardMathLibrary.TimeConstants.t5s;
import de.catstorm.trilife.logic.PlayerUtility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
        boolean shouldApplyResistance = true;
        for (var instance : effects) {
            var effect = new StatusEffectInstance(instance);
            owner.addStatusEffect(effect);
            if (effect.getEffectType().equals(StatusEffects.RESISTANCE)) shouldApplyResistance = false;
        }
        if (shouldApplyResistance) owner.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,
            t5s, 5, false, false, true));
        PlayerUtility.grantAdvancement(owner, "ono");
    }
}