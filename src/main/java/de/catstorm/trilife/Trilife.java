package de.catstorm.trilife;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.catstorm.trilife.item.HeartTotemItem;
import de.catstorm.trilife.item.TotemItem;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.PlayerLivesPayload;
import de.catstorm.trilife.records.PlayersAlivePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (key.toString().equals("ResourceKey[minecraft:loot_table / minecraft:entities/evoker]")) { //I really hope no one will question me
                LootPool.Builder poolBuilder = LootPool.builder()
                    .with(ItemEntry.builder(TrilifeItems.EMPTY_TOTEM));

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

                switch (playerState.lives) {
                    case 0 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                        "team leave " + entity.getName().getString());
                    case 1 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                        "team join reds " + entity.getName().getString());
                    case 2 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                        "team join yellows " + entity.getName().getString());
                    case 3 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                        "team join greens " + entity.getName().getString());
                }

                if (playerState.lives <= 0) {
                    server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                        "gamemode spectator " + entity.getName().getString());
                }

                ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(entity.getUuid());
                server.execute(() -> {
                    ServerPlayNetworking.send(playerEntity, new PlayerLivesPayload(playerState.lives));
                });
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("trilife")
                .then(CommandManager.literal("init")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(Trilife::trilifeInitCommand))
                .then(CommandManager.literal("setLives")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(CommandManager.argument("players", EntityArgumentType.players())
                        .then(CommandManager.argument("lives", IntegerArgumentType.integer())
                            .executes(Trilife::trilifeSetLivesCommand))))
                .then(CommandManager.literal("PvPZoneExempt")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(CommandManager.argument("players", EntityArgumentType.players())
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(Trilife::trilifeSetPvPExemptCommand)))));
        });

        TrilifeItems.initItems();
    }

    private static int trilifeInitCommand(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
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

            switch (playerState.lives) {
                case 1 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                        "team join reds " + player.getName().getString());
                case 2 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                    "team join yellows " + player.getName().getString());
                case 3 -> server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                    "team join greens " + player.getName().getString());
            }
        }
        return 0;
    }

    //TODO: fix this system
    private static int trilifeSetPvPExemptCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var players = EntityArgumentType.getPlayers(context, "players");
        var set = BoolArgumentType.getBool(context, "value");
        for (var player : players) {

        }
        return 0;
    }
}