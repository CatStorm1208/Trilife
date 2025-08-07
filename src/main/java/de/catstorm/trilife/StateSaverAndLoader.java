package de.catstorm.trilife;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.UUID;
import static de.catstorm.trilife.Trilife.MOD_ID;

public class StateSaverAndLoader extends PersistentState {
    public HashMap<UUID, PlayerData> players = new HashMap<>();
    public HashMap<UUID, Integer> playerLivesQueue = new HashMap<>();
    public String token = "";

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound playersNbt = new NbtCompound();

        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putInt("player_lives", playerData.lives);
            playerNbt.putInt("totem_popup", playerData.useless);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        NbtCompound livesQueueNbt = new NbtCompound();
        playerLivesQueue.forEach((uuid, change) -> {
            NbtCompound queueEntryNbt = new NbtCompound();

            queueEntryNbt.putInt("change", change);

            livesQueueNbt.put(uuid.toString(), queueEntryNbt);
        });
        nbt.put("player_lives_queue", livesQueueNbt);
        nbt.putString("token", token);

        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();

            playerData.lives = playersNbt.getCompound(key).getInt("player_lives");
            playerData.useless = playersNbt.getCompound(key).getInt("totem_popup");

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });

        NbtCompound livesQueueNbt = tag.getCompound("player_lives_queue");
        livesQueueNbt.getKeys().forEach(key -> state.playerLivesQueue.put(UUID.fromString(key),
            livesQueueNbt.getCompound(key).getInt("change")));

        state.token = tag.getString("token");

        return state;
    }

    public static StateSaverAndLoader createNew() {
        StateSaverAndLoader state = new StateSaverAndLoader();
        state.players = new HashMap<>();
        state.playerLivesQueue = new HashMap<>();
        state.token = "";
        return state;
    }

    private static final Type<StateSaverAndLoader> type = new Type<>(
        StateSaverAndLoader::createNew,
        StateSaverAndLoader::createFromNbt,
        null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;
        StateSaverAndLoader state = serverWorld.getPersistentStateManager().getOrCreate(type, MOD_ID);
        state.markDirty();

        return state;
    }

    public static PlayerData getPlayerState(LivingEntity player) {
        assert player.getWorld().getServer() != null;
        StateSaverAndLoader serverState = getServerState(player.getWorld().getServer());

        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
    }
}