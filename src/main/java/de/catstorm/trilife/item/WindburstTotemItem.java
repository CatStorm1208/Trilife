package de.catstorm.trilife.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Box;

import java.util.UUID;

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
            MinecraftServer server = owner.getServer();
            assert server != null;

            WindChargeEntity windCharge = EntityType.WIND_CHARGE.create(owner.getWorld());
            assert windCharge != null;
            windCharge.updatePosition(target.getX(), target.getY(), target.getZ());
            windCharge.setVelocity(0, -10, 0);

            owner.getWorld().spawnEntity(windCharge);
            windCharge.setUuid(UUID.randomUUID());
            owner.getWorld().spawnEntity(windCharge);
        }
    }
}