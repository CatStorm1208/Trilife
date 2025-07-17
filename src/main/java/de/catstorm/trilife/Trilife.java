package de.catstorm.trilife;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.LinkPlayersPayload;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.PlayersAlivePayload;
import de.catstorm.trilife.records.TotemFloatPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trilife implements ModInitializer {
    public static final String MOD_ID = "trilife";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initialised Trilife!");

        PayloadTypeRegistry.playS2C().register(PlayersAlivePayload.ID, PlayersAlivePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerLivesPayload.ID, PlayerLivesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TotemFloatPayload.ID, TotemFloatPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LinkPlayersPayload.ID, LinkPlayersPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(handler.getPlayer());

            server.execute(() -> {
                ServerPlayNetworking.send(handler.getPlayer(), new PlayerLivesPayload(playerState.lives));
            });
        });

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (key.toString().equals("ResourceKey[minecraft:loot_table / minecraft:entities/evoker]")) { //I really hope no one will question me
                LootPool.Builder poolBuilder = LootPool.builder().with(ItemEntry.builder(TrilifeItems.EMPTY_TOTEM));

                tableBuilder.pool(poolBuilder);
            }
            else if (key.toString().equals("ResourceKey[minecraft:loot_table / minecraft:chests/ancient_city_ice_box]")) {
                LootPool.Builder poolBuilder = LootPool.builder().with(ItemEntry.builder(Items.ACACIA_FENCE)); //NOTE: placeholder

                tableBuilder.pool(poolBuilder);
            }
        });

        //What the fuck am I even doing?
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity.isPlayer()) {
                MinecraftServer server = entity.getServer();
                assert server != null;

                PlayerData playerState = StateSaverAndLoader.getPlayerState(entity);
                playerState.lives -= 1;

                evalLives(entity, playerState.lives, server);

                ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(entity.getUuid());
                server.execute(() -> {
                    ServerPlayNetworking.send(playerEntity, new PlayerLivesPayload(playerState.lives));
                });

                //Kill Piss advancement
                if (damageSource.getSource() instanceof PlayerEntity) {
                    PlayerEntity killer = (PlayerEntity) damageSource.getSource();

                    assert entity.getServer() != null;
                    if (entity.getUuidAsString().equals("ff1337da-66b4-46af-bc1d-51714fb8f93d") ||
                        entity.getCommandTags().contains("trilife:pisstest")) {
                        entity.getServer().getCommandManager().executeWithPrefix(entity.getServer().getCommandSource(),
                            "advancement grant " + killer.getName().getString() + " only trilife:trilife/vecchios_saviour");
                    }
                    if (playerState.lives == 0) {
                        entity.getServer().getCommandManager().executeWithPrefix(entity.getServer().getCommandSource(),
                            "advancement grant " + killer.getName().getString() + " only trilife:trilife/the_end_is_never_the_end");
                    }
                }
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("trilife")
                .then(CommandManager.literal("link")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(Trilife::trilifeLinkCommand)))
                .then(CommandManager.literal("revive")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(Trilife::trilifeReviveCommand)))
                .then(CommandManager.literal("increment")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(Trilife::trilifeIncrementCommand)))
                .then(CommandManager.literal("init")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(Trilife::trilifeInitCommand))
                .then(CommandManager.literal("setLives")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(CommandManager.argument("players", EntityArgumentType.players())
                        .then(CommandManager.argument("lives", IntegerArgumentType.integer())
                            .executes(Trilife::trilifeSetLivesCommand)))));
        });

        TrilifeItems.initItems();
    }

    public static void evalLives(LivingEntity player, int lives, MinecraftServer server) {
        switch (lives) {
            case 0 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                "team leave " + player.getName().getString());
            case 1 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                "team join reds " + player.getName().getString());
            case 2 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                "team join yellows " + player.getName().getString());
            case 3 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                "team join greens " + player.getName().getString());
        }

        if (lives <= 0) {
            server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                "gamemode spectator " + player.getName().getString());
        }
        else {
            server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                "gamemode survival " + player.getName().getString());
        }
    }

    private static int trilifeLinkCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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

    private static int trilifeReviveCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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

                evalLives(revived, revivedState.lives, server);
                evalLives(executor, executorState.lives, server);

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

    private static int trilifeIncrementCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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

    private static int trilifeInitCommand(CommandContext<ServerCommandSource> context) {
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

    private static int trilifeSetLivesCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        for (var player : EntityArgumentType.getPlayers(context, "players")) {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(player);
            playerState.lives = IntegerArgumentType.getInteger(context, "lives");

            MinecraftServer server = player.getServer();
            assert server != null;

            ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
            server.execute(() -> {
                ServerPlayNetworking.send(playerEntity, new PlayerLivesPayload(playerState.lives));
            });

            evalLives(player, playerState.lives, server);
        }
        return 0;
    }
}