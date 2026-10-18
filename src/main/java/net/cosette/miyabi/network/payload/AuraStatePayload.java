package net.cosette.miyabi.network.payload;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

/** Serveur -> client : état de la battle aura pour l'icône du HUD (0 inactive, 1 active, 2 épuisée). */
public record AuraStatePayload(boolean awakened, int state) implements CustomPacketPayload {
    public static final Type<AuraStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "aura_state"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, AuraStatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, AuraStatePayload::awakened,
                    ByteBufCodecs.VAR_INT, AuraStatePayload::state,
                    AuraStatePayload::new
            );
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}