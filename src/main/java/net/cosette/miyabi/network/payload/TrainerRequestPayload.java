package net.cosette.miyabi.network.payload;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

/** Client -> serveur : action dans le menu d'un entraîneur (learn_start, rankup_start, claim, abandon, refresh). */
public record TrainerRequestPayload(String action, String arg) implements CustomPacketPayload {
    public static final Type<TrainerRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "trainer_request"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, TrainerRequestPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, TrainerRequestPayload::action,
                    ByteBufCodecs.STRING_UTF8, TrainerRequestPayload::arg,
                    TrainerRequestPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}