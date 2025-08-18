package de.catstorm.trilife;

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
import net.minecraft.world.GameMode;

public class TrilifeCommands {
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
        PlayerUtility.teamGen(server);

        server.setDefaultGameMode(GameMode.SURVIVAL);

        for (var player : server.getPlayerManager().getPlayerList()) {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(player);
            playerState.lives = 3;

            server.execute(() -> ServerPlayNetworking.send(player, new PlayerLivesPayload(playerState.lives)));

            PlayerUtility.evalLives(player, playerState.lives, server);

            PlayerUtility.grantAdvancement(player, "root");
        }

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