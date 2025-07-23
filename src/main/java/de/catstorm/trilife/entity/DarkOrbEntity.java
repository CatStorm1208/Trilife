package de.catstorm.trilife.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DarkOrbEntity extends AbstractWindChargeEntity {
    private int r = 3;

    public DarkOrbEntity(EntityType<? extends AbstractWindChargeEntity> entityType, World world) {
        super(entityType, world);
    }

    public DarkOrbEntity(PlayerEntity player, World world, double x, double y, double z) {
        super(TrilifeEntityTypes.DARK_ORB, world, player, x, y, z);
    }

    //Some insane shit ikr
    public DarkOrbEntity(World world, double x, double y, double z, Vec3d velocity) {
        this(TrilifeEntityTypes.DARK_ORB, world);
        this.refreshPositionAndAngles(x, y, z, this.getYaw(), this.getPitch());
        this.refreshPosition();
        this.setVelocity(velocity.normalize().multiply(accelerationPower));
        this.velocityDirty = true;
        this.accelerationPower = 0.0;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        bust(entityHitResult.getPos());
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        bust(blockHitResult.getPos());
    }

    @Override
    protected void createExplosion(Vec3d pos) {
    }

    protected void bust(Vec3d pos) {
        var targets = this.getWorld().getOtherEntities(this,
            new Box(pos.getX()-r, pos.getY()-r, pos.getZ()-r,
                    pos.getX()+r, pos.getY()+r, pos.getZ()+r));

        for (var target : targets) {
            if (target instanceof LivingEntity livingEntity) {
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 10*20, 0));
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 10*20, 1));
            }
        }
        discard();
    }
}
