package de.catstorm.trilife;

import de.catstorm.trilife.entity.TrilifeEntityTypes;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.PlayersAlivePayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import de.catstorm.trilife.sound.TrilifeSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class Trilife implements ModInitializer {
    public static final String MOD_ID = "trilife";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    protected static final HashMap<UUID, Integer> playerLivesQueue = new HashMap<>();
    public static final HashMap<UUID, Integer> playerLogoutZombies = new HashMap<>();
    public static final HashMap<UUID, Set<ItemStack>> zombieInventories = new HashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(PlayersAlivePayload.ID, PlayersAlivePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerLivesPayload.ID, PlayerLivesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TotemFloatPayload.ID, TotemFloatPayload.CODEC);

        TrilifeEvents.initEvents();
        TrilifeItems.initItems();
        TrilifeEntityTypes.initEntities();
        TrilifeSounds.initSounds();

        LOGGER.info("Initialised Trilife!");
    }

    public static void evalLives(LivingEntity player, int lives, MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        assert scoreboard != null;
        switch (lives) {
            case 0 -> {
                scoreboard.clearTeam(player.getNameForScoreboard());

                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(player.getWorld());
                assert lightning != null;
                lightning.updatePosition(player.getX(), player.getY(), player.getZ());
                player.getWorld().spawnEntity(lightning);
            }
            case 1 -> scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("reds"));
            case 2 -> scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("yellows"));
            case 3 -> scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("greens"));
            case 4 -> scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("blues"));
        }
        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (lives <= 0) serverPlayer.changeGameMode(GameMode.SPECTATOR);
            else serverPlayer.changeGameMode(GameMode.DEFAULT);
        }
    }

    public static boolean isPlayerOnline(UUID uuid, MinecraftServer server) {
        for (var player : server.getPlayerManager().getPlayerList()) {
            if (player.getUuid().equals(uuid)) return true;
        }
        return false;
    }

    public static void queuePlayerLivesChange(PlayerEntity player, int change) {
        queuePlayerLivesChange(player.getUuid(), change);
    }

    public static void queuePlayerLivesChange(UUID uuid, int change) {
        playerLivesQueue.put(uuid, playerLivesQueue.getOrDefault(uuid, 0) + change);
    }
}