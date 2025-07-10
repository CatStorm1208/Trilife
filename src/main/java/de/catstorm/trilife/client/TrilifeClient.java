package de.catstorm.trilife.client;

import de.catstorm.trilife.records.PlayerLivesPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import de.catstorm.trilife.records.PlayersAlivePayload;
import net.minecraft.text.Text;

public class TrilifeClient implements ClientModInitializer {
    private static void handlePlayersAlivePayload(PlayersAlivePayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        player.sendMessage(Text.of("Total players alive: " + payload.totalPlayersAlive()));
    }

    private static void handlePlayerLivesPayload(PlayerLivesPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        player.sendMessage(Text.of("Your Life count: " + payload.playerLifeCount())); //TODO: probably remove this line
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(PlayersAlivePayload.ID, TrilifeClient::handlePlayersAlivePayload);
        ClientPlayNetworking.registerGlobalReceiver(PlayerLivesPayload.ID, TrilifeClient::handlePlayerLivesPayload);
    }
}
