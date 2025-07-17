package de.catstorm.trilife.client;

import de.catstorm.trilife.hud.ThreeHeartsOverlay;
import de.catstorm.trilife.item.TotemItem;
import de.catstorm.trilife.records.LinkPlayersPayload;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import de.catstorm.trilife.records.PlayersAlivePayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class TrilifeClient implements ClientModInitializer {
    public static int lives = 0;

    private static void handlePlayersAlivePayload(PlayersAlivePayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        player.sendMessage(Text.of("Total players alive: " + payload.totalPlayersAlive()));
    }

    private static void handlePlayerLivesPayload(PlayerLivesPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        lives = payload.playerLifeCount();
        //player.sendMessage(Text.of("Your Life count: " + payload.playerLifeCount())); //TODO: probably remove this line
    }

    private static void handleTotemFloatPayload(TotemFloatPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        int useless = payload.useless(); //Pls don't ask why
        for (var item : player.getHandItems()) {
            if (item.getItem() instanceof TotemItem) {
                MinecraftClient.getInstance().gameRenderer.showFloatingItem(item);
                break;
            }
        }
    }

    private static void handleLinkPlayersPayload(LinkPlayersPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        assert player.getServer() != null;
        String link = payload.link();
        if (link.startsWith("ready")) {
            return;
        }

        //TODO: RELEASE ME!!!!!!!
        PlayerEntity sender = player.getServer().getPlayerManager().getPlayer(UUID.fromString(link.split(":")[1]));
        assert sender != null;
        if (link.startsWith("request:")) {
            player.sendMessage(Text.of(sender.getName().getString() + "wishes to link with you!\n'/trilife accept' to accept\n'/trilife deny' to deny"));
        }
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(PlayersAlivePayload.ID, TrilifeClient::handlePlayersAlivePayload);
        ClientPlayNetworking.registerGlobalReceiver(PlayerLivesPayload.ID, TrilifeClient::handlePlayerLivesPayload);
        ClientPlayNetworking.registerGlobalReceiver(TotemFloatPayload.ID, TrilifeClient::handleTotemFloatPayload);
        ClientPlayNetworking.registerGlobalReceiver(LinkPlayersPayload.ID, TrilifeClient::handleLinkPlayersPayload);

        HudRenderCallback.EVENT.register(new ThreeHeartsOverlay());
    }
}
