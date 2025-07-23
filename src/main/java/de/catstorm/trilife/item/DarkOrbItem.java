package de.catstorm.trilife.item;

import de.catstorm.trilife.entity.DarkOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.item.WindChargeItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class DarkOrbItem extends Item implements ProjectileItem {
    public DarkOrbItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            DarkOrbEntity darkOrbEntity = new DarkOrbEntity(user, world, user.getPos().getX(), user.getEyePos().getY(), user.getPos().getZ());
            darkOrbEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.5f, 1.0f);
            world.spawnEntity(darkOrbEntity);
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WIND_CHARGE_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        ItemStack itemStack = user.getStackInHand(hand);
        user.getItemCooldownManager().set(this, 10);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        itemStack.decrementUnlessCreative(1, user);
        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        Random random = world.getRandom();
        double d = random.nextTriangular((double)direction.getOffsetX(), 0.11485000000000001);
        double e = random.nextTriangular((double)direction.getOffsetY(), 0.11485000000000001);
        double f = random.nextTriangular((double)direction.getOffsetZ(), 0.11485000000000001);
        Vec3d vec3d = new Vec3d(d, e, f);
        DarkOrbEntity darkOrbEntity = new DarkOrbEntity(world, pos.getX(), pos.getY(), pos.getZ(), vec3d);
        darkOrbEntity.setVelocity(vec3d);
        return darkOrbEntity;
    }
}