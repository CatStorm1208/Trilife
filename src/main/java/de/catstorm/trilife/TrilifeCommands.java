package de.catstorm.trilife;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.catstorm.trilife.logic.PlayerUtility;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import static de.catstorm.trilife.Trilife.LOGGER;

public class TrilifeCommands {
    protected static int setEnabled(CommandContext<ServerCommandSource> context) {
        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getServer();

        StateSaverAndLoader state = StateSaverAndLoader.getServerState(server);

        if (enabled == state.enabled) {
            source.sendError(Text.of("Trilife is already " + (enabled? "enabled" : "disabled")));
            return -1;
        }
        state.enabled = enabled;

        for (var player : server.getPlayerManager().getPlayerList()) {
            server.execute(() -> ServerPlayNetworking.send(player,
                new PlayerLivesPayload(state.enabled? StateSaverAndLoader.getPlayerState(player).lives : 4)));
        }
        return 0;
    }

    protected static int increment(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = EntityArgumentType.getPlayer(context, "player");

        PlayerData playerState = StateSaverAndLoader.getPlayerState(player);
        playerState.lives += 1;

        MinecraftServer server = player.getServer();
        assert server != null;

        ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
        server.execute(() -> {
            assert playerEntity != null;
            ServerPlayNetworking.send(playerEntity, new PlayerLivesPayload(playerState.lives));
        });

        PlayerUtility.evalLives(player, playerState.lives, server);
        return 0;
    }

    protected static int init(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        LOGGER.info("There might be four warnings below, these can be ignored!");
        PlayerUtility.teamGen(server);

        server.setDefaultGameMode(GameMode.SURVIVAL);

        for (var player : server.getPlayerManager().getPlayerList()) {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(player);
            playerState.lives = 3;

            server.execute(() -> ServerPlayNetworking.send(player, new PlayerLivesPayload(playerState.lives)));

            PlayerUtility.evalLives(player, playerState.lives, server);

            PlayerUtility.grantAdvancement(player, "root");
        }

        StateSaverAndLoader.getServerState(server).enabled = true;
        return 0;
    }

    protected static int setLives(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        for (var player : EntityArgumentType.getPlayers(context, "players")) {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(player);
            playerState.lives = IntegerArgumentType.getInteger(context, "lives");

            MinecraftServer server = player.getServer();
            assert server != null;

            ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
            server.execute(() -> {
                assert playerEntity != null;
                ServerPlayNetworking.send(playerEntity, new PlayerLivesPayload(playerState.lives));
            });

            PlayerUtility.evalLives(player, playerState.lives, server);
        }
        return 0;
    }

    protected static int genfrosted(CommandContext<ServerCommandSource> context) {
        assert context.getSource().getPlayer() != null;
        PlayerData playerState = StateSaverAndLoader.getPlayerState(context.getSource().getPlayer());
        playerState.useless = 1;

        assert context.getSource().getServer() != null;
        context.getSource().getServer().execute(() -> ServerPlayNetworking.send(context.getSource().getPlayer(), new TotemFloatPayload(playerState.useless)));
        return 0;
    }
}