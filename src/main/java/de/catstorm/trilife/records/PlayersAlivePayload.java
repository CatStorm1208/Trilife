package de.catstorm.trilife.records;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import static de.catstorm.trilife.Trilife.MOD_ID;

public record PlayersAlivePayload(Integer totalPlayersAlive) implements CustomPayload {
    public static final Identifier PlayerLivesID = Identifier.of(MOD_ID, "players_alive");
    public static final CustomPayload.Id<PlayersAlivePayload> ID = new CustomPayload.Id<>(PlayerLivesID);
    public static final PacketCodec<PacketByteBuf, PlayersAlivePayload> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER, PlayersAlivePayload::totalPlayersAlive,
        PlayersAlivePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}