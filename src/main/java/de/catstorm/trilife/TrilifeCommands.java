package de.catstorm.trilife;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TrilifeCommands {
    //NOTE: AdvancementCommand.java
    protected static int link(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        assert player != null;
        var item = player.getInventory().main.get(player.getInventory().selectedSlot);
        if (item.isOf(TrilifeItems.LINKABLE_TOTEM)) {
            item = new ItemStack(TrilifeItems.LINKED_TOTEM);
            item.set(TrilifeItems.LINKED_PLAYER_COMPONENT, player.getUuidAsString());
            player.getInventory().setStack(player.getInventory().selectedSlot, item);

            context.getSource().getServer().getCommandManager().executeWithPrefix(context.getSource().getServer().getCommandSource(),
                "advancement grant " + player.getName().getString() + " only trilife:trilife/contract");
            return 0;
        }
        player.sendMessage(Text.of("You are not holding an unlinked Linked Totem!"));
        return 1;
    }

    protected static int revive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        assert context.getSource().getPlayer() != null;
        for (var item : context.getSource().getPlayer().getHandItems()) {
            if (item.isOf(TrilifeItems.SOUL_HEART)) {
                assert context.getSource().getPlayer() != null;

                ServerPlayerEntity revived = EntityArgumentType.getPlayer(context, "player");
                ServerPlayerEntity executor = context.getSource().getPlayer();
                PlayerData revivedState = StateSaverAndLoader.getPlayerState(revived);
                PlayerData executorState = StateSaverAndLoader.getPlayerState(executor);

                if (revivedState.lives != 0) {
                    executor.sendMessage(Text.of("This player is still alive!"));
                    return 1;
                }

                MinecraftServer server = context.getSource().getServer();
                assert server != null;

                revivedState.lives += 1;
                executorState.lives -= 1;

                Trilife.evalLives(revived, revivedState.lives, server);
                Trilife.evalLives(executor, executorState.lives, server);

                server.execute(() -> {
                    ServerPlayNetworking.send(revived, new PlayerLivesPayload(revivedState.lives));
                    ServerPlayNetworking.send(executor, new PlayerLivesPayload(executorState.lives));
                });

                revived.updatePosition(executor.getX(), executor.getY(), executor.getZ());
                server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                    "advancement grant " + revived.getName().getString() + " only trilife:trilife/im_alive_is_nice");
                server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                    "advancement grant " + executor.getName().getString() + " only trilife:trilife/necromancer");

                item.decrement(1);

                return 0;
            }
        }
        return 1;
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

        Trilife.evalLives(player, playerState.lives, server);
        return 0;
    }

    protected static int init(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
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

        for (var player : server.getPlayerManager().getPlayerList()) {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(player);
            playerState.lives = 3;

            server.execute(() -> ServerPlayNetworking.send(player, new PlayerLivesPayload(playerState.lives)));

            Trilife.evalLives(player, playerState.lives, server);
        }
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "advancement grant @a only trilife:trilife/root");

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

            Trilife.evalLives(player, playerState.lives, server);
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