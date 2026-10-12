package net.cosette.miyabi.network.payload;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

/** Serveur -> client : état du mana à afficher dans la barre. */
public record ManaSyncPayload(float current, float max, boolean awakened) implements CustomPacketPayload {
    public static final Type<ManaSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "mana_sync"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, ManaSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, ManaSyncPayload::current,
                    ByteBufCodecs.FLOAT, ManaSyncPayload::max,
                    ByteBufCodecs.BOOL, ManaSyncPayload::awakened,
                    ManaSyncPayload::new
            );
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}