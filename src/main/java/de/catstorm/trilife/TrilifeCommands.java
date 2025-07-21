package de.catstorm.trilife;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.LinkPlayersPayload;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TrilifeCommands {
    protected static int unlink(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        assert server != null;

        ServerPlayerEntity executor = context.getSource().getPlayer();
        assert executor != null;
        PlayerData executorState = StateSaverAndLoader.getPlayerState(executor);

        ServerPlayerEntity linked = server.getPlayerManager().getPlayer(executorState.link.split(":")[1]);
        assert linked != null;
        PlayerData linkedState = StateSaverAndLoader.getPlayerState(linked);

        executorState.link = "ready:unlink_sent";
        linkedState.link = "ready:unlink_received:" + executor.getName().getString();

        server.execute(() -> {
            ServerPlayNetworking.send(executor, new LinkPlayersPayload(executorState.link));
            ServerPlayNetworking.send(linked, new LinkPlayersPayload(linkedState.link));
        });
        return 0;
    }

    protected static int accept(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        assert server != null;
        assert context.getSource().getPlayer() != null;
        PlayerData acceptorSate = StateSaverAndLoader.getPlayerState(context.getSource().getPlayer());

        ServerPlayerEntity requester = server.getPlayerManager().getPlayer(acceptorSate.link.split(":")[1]);
        assert requester != null;
        PlayerData requesterState = StateSaverAndLoader.getPlayerState(requester);

        acceptorSate.link = "linked:" + requester.getUuidAsString();
        requesterState.link = "linked:" + requester.getUuidAsString();
        server.execute(() -> {
            ServerPlayNetworking.send(context.getSource().getPlayer(), new LinkPlayersPayload(acceptorSate.link));
            ServerPlayNetworking.send(requester, new LinkPlayersPayload(requesterState.link));
        });
        return 0;
    }

    protected static int deny(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        assert server != null;
        assert context.getSource().getPlayer() != null;
        PlayerData acceptorSate = StateSaverAndLoader.getPlayerState(context.getSource().getPlayer());

        ServerPlayerEntity requester = server.getPlayerManager().getPlayer(acceptorSate.link.split(":")[1]);
        assert requester != null;
        PlayerData requesterState = StateSaverAndLoader.getPlayerState(requester);

        acceptorSate.link = "ready:received";
        requesterState.link = "ready:sent";
        server.execute(() -> {
            ServerPlayNetworking.send(context.getSource().getPlayer(), new LinkPlayersPayload(acceptorSate.link));
            ServerPlayNetworking.send(requester, new LinkPlayersPayload(requesterState.link));
        });
        return 0;
    }

    protected static int link(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        assert server != null;
        assert context.getSource().getPlayer() != null;
        PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        if (context.getSource().getPlayer().getUuidAsString().equals(player.getUuidAsString())) {
            context.getSource().getPlayer().sendMessage(Text.of("You can't link a totem to yourself!"));
            return 1;
        }

        PlayerData receiverState = StateSaverAndLoader.getPlayerState(player);
        PlayerData senderState = StateSaverAndLoader.getPlayerState(context.getSource().getPlayer());
        if (!receiverState.link.startsWith("ready")) {
            context.getSource().getPlayer().sendMessage(Text.of("The receiver is not ready to link!"));
            return 1;
        }
        if (!senderState.link.startsWith("ready")) {
            context.getSource().getPlayer().sendMessage(Text.of("Please unlink before linking to another player"));
        }
        receiverState.link = "request:" + context.getSource().getPlayer().getUuidAsString();

        ServerPlayerEntity receiver = server.getPlayerManager().getPlayer(player.getUuid());
        ServerPlayerEntity sender = server.getPlayerManager().getPlayer(context.getSource().getPlayer().getUuid());
        server.execute(() -> {
            ServerPlayNetworking.send(receiver, new LinkPlayersPayload(receiverState.link));
            ServerPlayNetworking.send(sender, new LinkPlayersPayload("sent:" + player.getUuidAsString()));
        });
        return 0;
    }

    protected static int revive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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

                server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                    "tp " + revived.getName().getString() + " " + executor.getName().getString());
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
            ServerPlayNetworking.send(playerEntity, new PlayerLivesPayload(playerState.lives));
        });

        switch (playerState.lives) {
            case 1 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                "team join reds " + player.getName().getString());
            case 2 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                "team join yellows " + player.getName().getString());
            case 3 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                "team join greens " + player.getName().getString());
            case 4 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                "team join blues " + player.getName().getString());
        }
        return 0;
    }

    protected static int init(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        //blues
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team add blues");
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team modify blues friendlyFire true");
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team modify blues seeFriendlyInvisibles false");
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team modify blues color blue");

        //greens
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team add greens");
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team modify greens friendlyFire true");
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team modify greens seeFriendlyInvisibles false");
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team modify greens color green");

        //yellows
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team add yellows");
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team modify yellows friendlyFire true");
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team modify yellows seeFriendlyInvisibles false");
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team modify yellows color yellow");

        //reds
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team add reds");
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team modify reds friendlyFire true");
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team modify reds seeFriendlyInvisibles false");
        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "team modify reds color red");

        server.getCommandManager().executeWithPrefix(server.getCommandSource(),
            "trilife setLives @a 3");
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
        context.getSource().getServer().execute(() -> {
            ServerPlayNetworking.send(context.getSource().getPlayer(), new TotemFloatPayload(playerState.useless));
        });
        return 0;
    }
}