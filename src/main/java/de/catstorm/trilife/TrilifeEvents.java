package de.catstorm.trilife;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import static de.catstorm.trilife.Trilife.playerLivesQueue;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.PlayerLivesPayload;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import static de.catstorm.trilife.Trilife.playerLogoutZombies;
import static de.catstorm.trilife.Trilife.zombieInventories;

import java.util.HashSet;
import java.util.Set;

public class TrilifeEvents {
    public static void initEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            //TODO: zombie death handling
            PlayerData playerState = StateSaverAndLoader.getPlayerState(handler.getPlayer());
            if (playerLivesQueue.containsKey(handler.getPlayer().getUuid())) {
                playerState.lives += playerLivesQueue.get(handler.getPlayer().getUuid());
                playerLivesQueue.remove(handler.getPlayer().getUuid());
            }

            server.execute(() -> ServerPlayNetworking.send(handler.getPlayer(), new PlayerLivesPayload(playerState.lives)));
        });

        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            World world = player.getWorld();

            HuskEntity zombie = new HuskEntity(EntityType.HUSK, world);
            zombie.updatePosition(player.getX(), player.getY(), player.getZ());
            zombie.addCommandTag("ghost_" + player.getUuidAsString());
            zombie.setAiDisabled(true);
            zombie.setCustomName(Text.of(player.getNameForScoreboard()));
            world.spawnEntity(zombie);

            playerLogoutZombies.put(player.getUuid(), server.getTicks() + 60*20); //TODO: 3600*20

            Set<ItemStack> drops = new HashSet<>();
            drops.addAll(player.getInventory().main);
            drops.addAll(player.getInventory().armor);
            drops.addAll(player.getInventory().offHand);
            zombieInventories.put(player.getUuid(), drops);
        }));

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

                Trilife.evalLives(entity, playerState.lives, server);

                ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(entity.getUuid());
                server.execute(() -> {
                    assert playerEntity != null;
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
    }
}