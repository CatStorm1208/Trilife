package de.catstorm.trilife;

import de.catstorm.trilife.block.TrilifeBlocks;
import de.catstorm.trilife.block.blockEntity.TrilifeBlockEntityTypes;
import de.catstorm.trilife.entity.TrilifeEntityTypes;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.logic.PlayerUtility;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.RevivePlayerPayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import de.catstorm.trilife.sound.TrilifeSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class Trilife implements ModInitializer {
    public static final String MOD_ID = "trilife";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final HashMap<UUID, Integer> playerLogoutZombies = new HashMap<>();
    public static final HashMap<UUID, Set<ItemStack>> zombieInventories = new HashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(PlayerLivesPayload.ID, PlayerLivesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TotemFloatPayload.ID, TotemFloatPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(RevivePlayerPayload.ID, RevivePlayerPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RevivePlayerPayload.ID, (payload, context) -> {
            MinecraftServer server = context.server();
            PlayerUtility.revivePlayer(context.player(), server.getPlayerManager().getPlayer(payload.player()), server);
        });

        TrilifeEvents.initEvents();
        TrilifeComponents.initComponents();
        TrilifeItems.initItems();
        TrilifeBlocks.initBlocks();
        TrilifeBlockEntityTypes.initBlockEntities();
        TrilifeEntityTypes.initEntities();
        TrilifeSounds.initSounds();

        LOGGER.info("Initialised Trilife commons");
    }
}