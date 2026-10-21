package net.cosette.miyabi.network.payload;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

/**
 * Serveur -> client : début, resynchronisation ou fin de l'affichage de l'incantation.
 * Un texte vide arrête l'affichage. progressTicks : avancement de la charge au moment de l'envoi (après une interruption, il recule).
 */
public record IncantationPayload(String text, int durationTicks, int progressTicks) implements CustomPacketPayload {
    public static final Type<IncantationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "incantation"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, IncantationPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, IncantationPayload::text,
                    ByteBufCodecs.VAR_INT, IncantationPayload::durationTicks,
                    ByteBufCodecs.VAR_INT, IncantationPayload::progressTicks,
                    IncantationPayload::new
            );
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}