package de.catstorm.trilife.records;

import static de.catstorm.trilife.Trilife.MOD_ID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TotemFloatPayload(Integer useless) implements CustomPayload {
    public static final Identifier TotemFloatID = Identifier.of(MOD_ID, "totem_popup");
    public static final CustomPayload.Id<TotemFloatPayload> ID = new CustomPayload.Id<>(TotemFloatID);
    public static final PacketCodec<PacketByteBuf, TotemFloatPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER, TotemFloatPayload::useless,
        TotemFloatPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}