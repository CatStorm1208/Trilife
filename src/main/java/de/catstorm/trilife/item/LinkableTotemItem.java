package de.catstorm.trilife.item; //de.catstorm.trilife.item.totem is only for items that extend TotemItem ig

import de.catstorm.trilife.Trilife;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class LinkableTotemItem extends Item {
    public LinkableTotemItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (user instanceof ServerPlayerEntity player) {
            itemStack = new ItemStack(TrilifeItems.LINKED_TOTEM);
            itemStack.set(TrilifeItems.LINKED_PLAYER_COMPONENT, player.getUuidAsString());
            player.getInventory().setStack(player.getInventory().selectedSlot, itemStack);

            Trilife.grantAdvancement(player, "contract");
        }
        return TypedActionResult.pass(itemStack);
    }
}