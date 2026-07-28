package net.cosette.miyabi.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

public record CastInputPayload(int slot, int action) implements CustomPacketPayload {

    public static final int ACTION_START = 0;
    public static final int ACTION_KEEPALIVE = 1;
    public static final int ACTION_STOP = 2;

    public static final Type<CastInputPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "cast_input"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, CastInputPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, CastInputPayload::slot,
                    ByteBufCodecs.VAR_INT, CastInputPayload::action,
                    CastInputPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}