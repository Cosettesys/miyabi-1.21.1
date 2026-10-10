package net.cosette.miyabi.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

public record SlotAssignPayload(int slot, String spellId) implements CustomPacketPayload {
    public static final Type<SlotAssignPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "slot_assign"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, SlotAssignPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SlotAssignPayload::slot,
                    ByteBufCodecs.STRING_UTF8, SlotAssignPayload::spellId,
                    SlotAssignPayload::new
            );
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}