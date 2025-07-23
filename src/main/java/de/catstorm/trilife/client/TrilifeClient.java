package de.catstorm.trilife.client;

import de.catstorm.trilife.entity.TrilifeEntityTypes;
import de.catstorm.trilife.hud.ThreeHeartsOverlay;
import de.catstorm.trilife.item.TotemItem;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import de.catstorm.trilife.records.PlayersAlivePayload;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.WindChargeEntityModel;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import static de.catstorm.trilife.Trilife.MOD_ID;

public class TrilifeClient implements ClientModInitializer {
    public static int lives = 0;
    public static final EntityModelLayer DARK_ORB_LAYER = new EntityModelLayer(Identifier.of(MOD_ID, "dark_orb"), "main");

    private static void handlePlayersAlivePayload(PlayersAlivePayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        player.sendMessage(Text.of("Total players alive: " + payload.totalPlayersAlive()));
    }

    private static void handlePlayerLivesPayload(PlayerLivesPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        lives = payload.playerLifeCount();
        //player.sendMessage(Text.of("Your Life count: " + payload.playerLifeCount()));
    }

    private static void handleTotemFloatPayload(TotemFloatPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        int useless = payload.useless(); //Pls don't ask why
        if (useless == 0) for (var item : player.getHandItems()) {
            if (item.getItem() instanceof TotemItem) {
                MinecraftClient.getInstance().gameRenderer.showFloatingItem(item);
                break;
            }
        }
        else if (useless == 1) {
            MinecraftClient.getInstance().gameRenderer.showFloatingItem(new ItemStack(TrilifeItems.GENFROSTED));
        }
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(PlayersAlivePayload.ID, TrilifeClient::handlePlayersAlivePayload);
        ClientPlayNetworking.registerGlobalReceiver(PlayerLivesPayload.ID, TrilifeClient::handlePlayerLivesPayload);
        ClientPlayNetworking.registerGlobalReceiver(TotemFloatPayload.ID, TrilifeClient::handleTotemFloatPayload);

        HudRenderCallback.EVENT.register(new ThreeHeartsOverlay());

        EntityRendererRegistry.register(TrilifeEntityTypes.DARK_ORB, DarkOrbRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(DARK_ORB_LAYER, WindChargeEntityModel::getTexturedModelData);
    }
}