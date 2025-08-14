package de.catstorm.trilife.logic;

import de.catstorm.trilife.StateSaverAndLoader;
import de.catstorm.trilife.Trilife;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import java.util.UUID;

public class PlayerUtility {
    public static void evalLives(LivingEntity player, int lives, MinecraftServer server) {
        evalLives(player, lives, server, true);
    }

    private static void evalLives(LivingEntity player, int lives, MinecraftServer server, boolean firstRun) {
        try {
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
        catch (Exception e) {
            if (firstRun) {
                Trilife.LOGGER.warn("Evaluating lives failed, this is normal during first world creation. Running team generation and retrying.");
                teamGen(server);
                evalLives(player, lives, server, false);
            }
            else {
                Trilife.LOGGER.error("A serious problem occurred whilst evaluating lives.");
                e.printStackTrace();
            }
        }
    }

    public static void grantAdvancement(LivingEntity player, String advancement) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            //Dodgy ahhhh code, but seems to work
            assert serverPlayer.getServer() != null;
            AdvancementEntry entry = serverPlayer.getServer().getAdvancementLoader().get(Identifier.of(Trilife.MOD_ID, "trilife/" + advancement));
            AdvancementProgress progress = serverPlayer.getAdvancementTracker().getProgress(entry);
            if (!progress.isDone()) for (String s : progress.getUnobtainedCriteria()) {
                serverPlayer.getAdvancementTracker().grantCriterion(entry, s);
            }
        }
    }

    public static boolean isPlayerOnline(UUID uuid, MinecraftServer server) {
        for (var player : server.getPlayerManager().getPlayerList()) {
            if (player.getUuid().equals(uuid)) return true;
        }
        return false;
    }

    public static void queuePlayerLivesChange(UUID uuid, int change, MinecraftServer server) {
        StateSaverAndLoader state = StateSaverAndLoader.getServerState(server);
        state.playerLivesQueue.put(uuid, state.playerLivesQueue.getOrDefault(uuid, 0) + change);
    }

    public static void teamGen(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();

        Team blues = scoreboard.addTeam("blues");
        blues.setFriendlyFireAllowed(true);
        blues.setShowFriendlyInvisibles(false);
        blues.setColor(Formatting.BLUE);

        Team greens = scoreboard.addTeam("greens");
        greens.setFriendlyFireAllowed(true);
        greens.setShowFriendlyInvisibles(false);
        greens.setColor(Formatting.GREEN);

        Team yellows = scoreboard.addTeam("yellows");
        yellows.setFriendlyFireAllowed(true);
        yellows.setShowFriendlyInvisibles(false);
        yellows.setColor(Formatting.YELLOW);

        Team reds = scoreboard.addTeam("reds");
        reds.setFriendlyFireAllowed(true);
        reds.setShowFriendlyInvisibles(false);
        reds.setColor(Formatting.RED);
    }
}