package de.catstorm.trilife;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.logic.PlayerUtility;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import de.catstorm.trilife.sound.TrilifeSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.EnumSet;

public class TrilifeCommands {
    @Deprecated
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

                PlayerUtility.evalLives(revived, revivedState.lives, server);
                PlayerUtility.evalLives(executor, executorState.lives, server);

                server.execute(() -> {
                    ServerPlayNetworking.send(revived, new PlayerLivesPayload(revivedState.lives));
                    ServerPlayNetworking.send(executor, new PlayerLivesPayload(executorState.lives));
                });

                revived.teleport((ServerWorld) revived.getWorld(), executor.getX(), executor.getY(), executor.getZ(),
                    EnumSet.noneOf(PositionFlag.class), executor.getYaw(), executor.getPitch());
                PlayerUtility.grantAdvancement(revived, "im_alive_is_nice");
                PlayerUtility.grantAdvancement(executor, "necromancer");

                item.decrement(1);

                revived.networkHandler.sendPacket(new PlaySoundS2CPacket(
                    RegistryEntry.of(TrilifeSounds.REVIVE_PLAYER), SoundCategory.NEUTRAL,
                    revived.getX(), revived.getY(), revived.getZ(), 1f, 1f,
                    revived.getWorld().getRandom().nextLong()));

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

        PlayerUtility.evalLives(player, playerState.lives, server);
        return 0;
    }

    protected static int init(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        PlayerUtility.teamGen(server);

        for (var player : server.getPlayerManager().getPlayerList()) {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(player);
            playerState.lives = 3;

            server.execute(() -> ServerPlayNetworking.send(player, new PlayerLivesPayload(playerState.lives)));

            PlayerUtility.evalLives(player, playerState.lives, server);

            PlayerUtility.grantAdvancement(player, "root");
        }

        server.setDefaultGameMode(GameMode.SURVIVAL);

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