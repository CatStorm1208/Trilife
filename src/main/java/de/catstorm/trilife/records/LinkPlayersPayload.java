package de.catstorm.trilife.records;

import static de.catstorm.trilife.Trilife.MOD_ID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record LinkPlayersPayload(String link) implements CustomPayload {
    public static final Identifier LinkPlayersID = Identifier.of(MOD_ID, "link_players");
    public static final CustomPayload.Id<LinkPlayersPayload> ID = new CustomPayload.Id<>(LinkPlayersID);
    public static final PacketCodec<PacketByteBuf, LinkPlayersPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, LinkPlayersPayload::link,
        LinkPlayersPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}