package de.catstorm.trilife.records;

import static de.catstorm.trilife.Trilife.MOD_ID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RevivePlayerPayload(String player) implements CustomPayload {
    public static final Identifier RevivePlayerID = Identifier.of(MOD_ID, "revive_player");
    public static final CustomPayload.Id<RevivePlayerPayload> ID = new CustomPayload.Id<>(RevivePlayerID);
    public static final PacketCodec<PacketByteBuf, RevivePlayerPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, RevivePlayerPayload::player,
        RevivePlayerPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}