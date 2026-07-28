package net.cosette.miyabi.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

public record SilentCastTogglePayload() implements CustomPacketPayload {

    public static final Type<SilentCastTogglePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "silent_cast_toggle"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, SilentCastTogglePayload> STREAM_CODEC =
            StreamCodec.unit(new SilentCastTogglePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}