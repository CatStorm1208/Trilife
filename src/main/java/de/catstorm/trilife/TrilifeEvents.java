package de.catstorm.trilife;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import static de.catstorm.trilife.Trilife.*;
import de.catstorm.trilife.item.TrilifeItems;
import de.catstorm.trilife.records.PlayerLivesPayload;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class TrilifeEvents {
    public static void initEvents() {
        ServerPlayConnectionEvents.JOIN.register(TrilifeEvents::handleServerPlayConnectionJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(TrilifeEvents::handleServerPlayConnectionDisconnect);
        LootTableEvents.MODIFY.register(TrilifeEvents::handleLootTableModify);
        ServerLivingEntityEvents.AFTER_DEATH.register(TrilifeEvents::handleLivingEntityAfterDeath);
        CommandRegistrationCallback.EVENT.register(TrilifeEvents::handleCommandRegistration);
    }

    private static void handleServerPlayConnectionJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        //TODO?: discard zombie on player join
        PlayerData playerState = StateSaverAndLoader.getPlayerState(handler.getPlayer());
        StateSaverAndLoader state = StateSaverAndLoader.getServerState(server);
        if (state.playerLivesQueue.containsKey(handler.getPlayer().getUuid())) {
            final int livesBefore = playerState.lives;
            playerState.lives += state.playerLivesQueue.get(handler.getPlayer().getUuid());
            if (livesBefore > playerState.lives) {
                var pos = handler.getPlayer().getRespawnTarget(false, TeleportTarget.NO_OP).pos();
                handler.getPlayer().setPos(pos.getX(), pos.getY(), pos.getZ());
                handler.getPlayer().getInventory().clear();
                handler.getPlayer().sendMessage(Text.of("You were killed whilst being logged out. A life has been deducted!"));
            }
            state.playerLivesQueue.remove(handler.getPlayer().getUuid());
        }

        server.execute(() -> ServerPlayNetworking.send(handler.getPlayer(), new PlayerLivesPayload(playerState.lives)));
        evalLives(handler.getPlayer(), playerState.lives, server);
    }

    private static void handleServerPlayConnectionDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();
        World world = player.getWorld();

        HuskEntity zombie = new HuskEntity(EntityType.HUSK, world);
        zombie.updatePosition(player.getX(), player.getY(), player.getZ());
        zombie.addCommandTag("ghost_" + player.getUuidAsString());
        zombie.setAiDisabled(true);
        zombie.setCustomName(Text.of(player.getNameForScoreboard()));
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnEntity(zombie);
        }

        playerLogoutZombies.put(player.getUuid(), server.getTicks() + 60*20); //NOTE: 3600*20

        Set<ItemStack> drops = new HashSet<>();
        drops.addAll(player.getInventory().main);
        drops.addAll(player.getInventory().armor);
        drops.addAll(player.getInventory().offHand);
        zombieInventories.put(player.getUuid(), drops);
    }

    private static void handleLootTableModify(RegistryKey<LootTable> key, LootTable.Builder tableBuilder,
                                              LootTableSource source, RegistryWrapper.WrapperLookup registries) {
        if (key.toString().equals("ResourceKey[minecraft:loot_table / minecraft:entities/evoker]")) { //I really hope no one will question me
            LootPool.Builder poolBuilder = LootPool.builder().with(ItemEntry.builder(TrilifeItems.EMPTY_TOTEM));
            tableBuilder.pool(poolBuilder);
        }
        else if (key.toString().equals("ResourceKey[minecraft:loot_table / minecraft:entities/pillager]")) {
            LootPool.Builder poolBuilder = LootPool.builder().with(ItemEntry.builder(TrilifeItems.TOTINIUM_NUGGET))
                .conditionally(RandomChanceLootCondition.builder(0.02f).build());
            tableBuilder.pool(poolBuilder);
        }
        else if (key.toString().equals("ResourceKey[minecraft:loot_table / minecraft:entities/vindicator]")) {
            LootPool.Builder poolBuilder = LootPool.builder().with(ItemEntry.builder(TrilifeItems.TOTINIUM_NUGGET))
                .conditionally(RandomChanceLootCondition.builder(0.05f).build());
            tableBuilder.pool(poolBuilder);
        }
        else if (key.toString().equals("ResourceKey[minecraft:loot_table / minecraft:chests/ancient_city_ice_box]")) {
            LootPool.Builder poolBuilder = LootPool.builder().with(ItemEntry.builder(TrilifeItems.DARK_ORB));
            tableBuilder.pool(poolBuilder);
        }
    }

    //What the fuck am I even doing?
    private static void handleLivingEntityAfterDeath(LivingEntity entity, DamageSource damageSource) {
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
                    Trilife.grantAdvancement(killer, "vecchios_saviour");
                }
                if (playerState.lives == 0) {
                    Trilife.grantAdvancement(killer, "the_end_is_never_the_end");
                }
            }
        }
    }

    private static void handleCommandRegistration(CommandDispatcher<ServerCommandSource> dispatcher,
                                                  CommandRegistryAccess registryAccess,
                                                  CommandManager.RegistrationEnvironment environment) {
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
    }
}