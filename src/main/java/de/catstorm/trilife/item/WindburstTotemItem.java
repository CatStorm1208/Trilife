package de.catstorm.trilife.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Box;

public class WindburstTotemItem extends TotemItem {
    public WindburstTotemItem(Settings settings, float health, StatusEffectInstance... effects) {
        super(settings, health, effects);
    }

    @Override
    public void onPop(DamageSource source, LivingEntity owner) {
        super.onPop(source, owner);

        var targets = owner.getWorld().getOtherEntities(owner,
            new Box(owner.getX()-10, owner.getY()-10, owner.getZ()-10,
                    owner.getX()+10, owner.getY()+10, owner.getZ()+10));

        for (var target : targets) {
            owner.getServer().getCommandManager().executeWithPrefix(owner.getServer().getCommandSource(),
                "execute as " + target.getUuidAsString() + " at @s run summon wind_charge ~ ~ ~ {Motion:[0.0,-10.0,0.0]}");
            owner.getServer().getCommandManager().executeWithPrefix(owner.getServer().getCommandSource(),
                "execute as " + target.getUuidAsString() + " at @s run summon wind_charge ~ ~ ~ {Motion:[0.0,-10.0,0.0]}");
        }
    }
}