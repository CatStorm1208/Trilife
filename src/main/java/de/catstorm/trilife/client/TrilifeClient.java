package de.catstorm.trilife.client;

import com.mojang.blaze3d.systems.RenderSystem;
import static de.catstorm.trilife.Trilife.MOD_ID;
import de.catstorm.trilife.TrilifeEvents;
import de.catstorm.trilife.entity.TrilifeEntityTypes;
import de.catstorm.trilife.item.TotemItem;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.PlayersAlivePayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.WindChargeEntityModel;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TrilifeClient implements ClientModInitializer {
    public static int lives = 0;
    public static final EntityModelLayer DARK_ORB_LAYER = new EntityModelLayer(Identifier.of(MOD_ID, "dark_orb"), "main");
    private static final Identifier EMPTY_HEART = Identifier.ofVanilla("hud/heart/container");
    private static final Identifier FILLED_HEART = Identifier.of("minecraft", "hud/heart/frozen_full"); //TODO: replace this
    private static final Identifier FOURTH_HEART = Identifier.of("minecraft", "hud/heart/poisoned_full"); //TODO: and this

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

        EntityRendererRegistry.register(TrilifeEntityTypes.DARK_ORB, DarkOrbRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(DARK_ORB_LAYER, WindChargeEntityModel::getTexturedModelData);

        TrilifeEvents.initClientEvents();
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        int x = (context.getScaledWindowWidth() / 2) - 5;
        int y = context.getScaledWindowHeight() - 31 - 4 - 20; //Ik this looks fucked, but don't touch it
        RenderSystem.enableBlend();
        if (lives < 4) {
            int offset = 12;

            context.drawGuiTexture(EMPTY_HEART, x, y-offset, 9, 9);
            if (lives >= 3) context.drawGuiTexture(FILLED_HEART, x, y-offset, 9, 9);

            context.drawGuiTexture(EMPTY_HEART, x+(offset/2), y, 9, 9);
            if (lives >= 2) context.drawGuiTexture(FILLED_HEART, x+(offset/2), y, 9, 9);

            context.drawGuiTexture(EMPTY_HEART, x-(offset/2), y, 9, 9);
            if (lives >= 1) context.drawGuiTexture(FILLED_HEART, x-(offset/2), y, 9, 9);
        }
        else {
            context.drawGuiTexture(EMPTY_HEART, x-5, y-24, 18, 18);
            context.drawGuiTexture(FOURTH_HEART, x-5, y-24, 18, 18);
        }
        RenderSystem.disableBlend();
    }
}