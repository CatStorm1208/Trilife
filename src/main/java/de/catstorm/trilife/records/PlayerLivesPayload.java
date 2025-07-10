package de.catstorm.trilife.records;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import static de.catstorm.trilife.Trilife.MOD_ID;

public record PlayerLivesPayload(Integer playerLifeCount) implements CustomPayload {
    public static final Identifier PlayerLivesID = Identifier.of(MOD_ID, "player_lives");
    public static final CustomPayload.Id<PlayerLivesPayload> ID = new CustomPayload.Id<>(PlayerLivesID);
    public static final PacketCodec<PacketByteBuf, PlayerLivesPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER, PlayerLivesPayload::playerLifeCount,
        PlayerLivesPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}