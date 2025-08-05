package de.catstorm.trilife.client;

import static de.catstorm.trilife.Trilife.LOGGER;
import static de.catstorm.trilife.Trilife.MOD_ID;
import de.catstorm.trilife.block.TrilifeBlocks;
import de.catstorm.trilife.entity.TrilifeEntityTypes;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.WindChargeEntityModel;
import net.minecraft.util.Identifier;

public class TrilifeClient implements ClientModInitializer {
    public static int lives = 0;
    public static final EntityModelLayer DARK_ORB_LAYER = new EntityModelLayer(Identifier.of(MOD_ID, "dark_orb"), "main");
    public static boolean animationCanStart = true;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(PlayerLivesPayload.ID, ClientPayloadHandler::handlePlayerLivesPayload);
        ClientPlayNetworking.registerGlobalReceiver(TotemFloatPayload.ID, ClientPayloadHandler::handleTotemFloatPayload);

        EntityRendererRegistry.register(TrilifeEntityTypes.DARK_ORB, DarkOrbRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(DARK_ORB_LAYER, WindChargeEntityModel::getTexturedModelData);

        TrilifeClientEvents.initClientEvents();

        BlockRenderLayerMap.INSTANCE.putBlock(TrilifeBlocks.TOTEM_VAULT, RenderLayer.getCutout());

        LOGGER.info("Initialised Trilife client");
    }
}