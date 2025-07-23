package de.catstorm.trilife;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.catstorm.trilife.entity.TrilifeEntityTypes;
import de.catstorm.trilife.item.TrilifeItems;
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
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.UUID;

public class Trilife implements ModInitializer {
    public static final String MOD_ID = "trilife";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final HashMap<UUID, Integer> playerLivesQueue = new HashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(PlayersAlivePayload.ID, PlayersAlivePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerLivesPayload.ID, PlayerLivesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TotemFloatPayload.ID, TotemFloatPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(handler.getPlayer());
            if (playerLivesQueue.containsKey(handler.getPlayer().getUuid())) {
                playerState.lives += playerLivesQueue.get(handler.getPlayer().getUuid());
                playerLivesQueue.remove(handler.getPlayer().getUuid());
            }

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
                LootPool.Builder poolBuilder = LootPool.builder().with(ItemEntry.builder(TrilifeItems.DARK_ORB));

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
                if (damageSource.getSource() instanceof PlayerEntity killer) {
                    assert entity.getServer() != null;
                    if (entity.getUuidAsString().equals("ff1337da-66b4-46af-bc1d-51714fb8f93d") ||
                        entity.getCommandTags().contains("trilife_pisstest")) {
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
                    .executes(TrilifeCommands::link))
                .then(CommandManager.literal("revive")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(TrilifeCommands::revive)))
                .then(CommandManager.literal("increment")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(TrilifeCommands::increment)))
                .then(CommandManager.literal("init")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(TrilifeCommands::init))
                .then(CommandManager.literal("setLives")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(CommandManager.argument("players", EntityArgumentType.players())
                        .then(CommandManager.argument("lives", IntegerArgumentType.integer())
                            .executes(TrilifeCommands::setLives)))));
            dispatcher.register(CommandManager.literal("genfrosted")
                .executes(TrilifeCommands::genfrosted));
        });

        TrilifeItems.initItems();
        TrilifeEntityTypes.initEntities();

        LOGGER.info("Initialised Trilife! Also, ignore the error above lol.");
    }

    public static void evalLives(LivingEntity player, int lives, MinecraftServer server) {
        switch (lives) {
            case 0 -> {
                server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                "team leave " + player.getName().getString());
                server.getCommandManager().executeWithPrefix(server.getCommandSource(),
                    "execute as " + player.getName().getString() + " at @s run summon lightning_bolt ~ ~ ~");
            }
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

    public static boolean isPlayerOnline(String uuid, MinecraftServer server) {
        return isPlayerOnline(UUID.fromString(uuid), server);
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