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
    public Integer totalPlayersAlive = 0;
    public HashMap<UUID, PlayerData> players = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("players_alive", totalPlayersAlive);

        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putInt("player_lives", playerData.lives);
            playerNbt.putInt("totem_popup", playerData.useless);
            playerNbt.putString("link_players", playerData.link);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        state.totalPlayersAlive = tag.getInt("players_alive");

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();

            playerData.lives = playersNbt.getCompound(key).getInt("player_lives");
            playerData.useless = playersNbt.getCompound(key).getInt("totem_popup");
            playerData.link = playersNbt.getCompound(key).getString("link_players");

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });

        return state;
    }

    public static StateSaverAndLoader createNew() {
        StateSaverAndLoader state = new StateSaverAndLoader();
        state.totalPlayersAlive = 0;
        state.players = new HashMap<>();
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
        StateSaverAndLoader serverState = getServerState(player.getWorld().getServer());
        PlayerData playerState = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());

        return playerState;
    }
}