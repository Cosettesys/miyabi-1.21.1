package net.cosette.miyabi.network.payload;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

/** Client -> serveur : touche de la battle aura (activer/désactiver) ou molette (changer de niveau). */
public record AuraInputPayload(int action, int value) implements CustomPacketPayload {
    public static final int ACTION_TOGGLE = 0;
    public static final int ACTION_SCROLL = 1; // value = +1 ou -1
    public static final Type<AuraInputPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "aura_input"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, AuraInputPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, AuraInputPayload::action,
                    ByteBufCodecs.VAR_INT, AuraInputPayload::value,
                    AuraInputPayload::new
            );
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}