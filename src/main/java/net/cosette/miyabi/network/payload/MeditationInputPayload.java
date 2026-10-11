package net.cosette.miyabi.network.payload;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

public record MeditationInputPayload(int action) implements CustomPacketPayload {

    public static final int ACTION_START = 0;
    public static final int ACTION_KEEPALIVE = 1;
    public static final int ACTION_STOP = 2;
    public static final Type<MeditationInputPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "meditation_input"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, MeditationInputPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(MeditationInputPayload::new, MeditationInputPayload::action);
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}