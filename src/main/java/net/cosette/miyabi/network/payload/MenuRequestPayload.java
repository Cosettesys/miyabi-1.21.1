package net.cosette.miyabi.network.payload;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

public record MenuRequestPayload() implements CustomPacketPayload {
    public static final Type<MenuRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "menu_request"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, MenuRequestPayload> STREAM_CODEC =
            StreamCodec.unit(new MenuRequestPayload());
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}