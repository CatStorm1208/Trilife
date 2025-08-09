package de.catstorm.trilife.item;

import de.catstorm.trilife.client.SoulHeartDialogue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SoulHeartItem extends Item {
    SoulHeartDialogue dialogue = new SoulHeartDialogue(Text.literal("Soul Heart revive dialogue"));

    public SoulHeartItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(dialogue);
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}