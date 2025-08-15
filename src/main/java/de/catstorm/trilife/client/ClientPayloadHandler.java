package de.catstorm.trilife.client;

import de.catstorm.trilife.item.totem.TotemItem;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ClientPayloadHandler {
    //Too lazy to check whether this warning is justified or not
    @SuppressWarnings("FieldCanBeLocal") private static SoulHeartDialogue dialogue;

    protected static void handlePlayerLivesPayload(PlayerLivesPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        TrilifeClient.lives = payload.playerLifeCount();
        //player.sendMessage(Text.of("Your Life count: " + payload.playerLifeCount()));
    }

    protected static void handleTotemFloatPayload(TotemFloatPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        int useless = payload.useless(); //Pls don't ask why
        switch (useless) {
            case 0 -> {
                for (var item : player.getHandItems()) if (item.getItem() instanceof TotemItem){
                    MinecraftClient.getInstance().gameRenderer.showFloatingItem(item);
                    break;
                }
            }
            case 1 -> MinecraftClient.getInstance().gameRenderer.showFloatingItem(new ItemStack(TrilifeItems.GENFROSTED));
            case 2 -> {
                dialogue = new SoulHeartDialogue(Text.literal("Soul Heart dialogue"));
                MinecraftClient.getInstance().setScreen(dialogue);
            }
            case 3 -> TrilifeClient.animationCanStart = true;
        }
    }
}