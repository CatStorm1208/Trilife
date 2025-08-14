package de.catstorm.trilife.item;

import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Objects;

public class SoulHeartItem extends Item {
    public SoulHeartItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity serverPlayer) {
            Objects.requireNonNull(serverPlayer.getServer()).execute(() ->
                ServerPlayNetworking.send(serverPlayer, new TotemFloatPayload(2)));
            return TypedActionResult.success(user.getStackInHand(hand));
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}