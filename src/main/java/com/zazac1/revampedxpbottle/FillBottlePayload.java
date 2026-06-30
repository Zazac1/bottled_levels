package com.zazac1.revampedxpbottle;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FillBottlePayload() implements CustomPacketPayload {
    public static final Type<FillBottlePayload> TYPE = new Type<>(RevampedXpBottleMod.id("fill_bottle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FillBottlePayload> CODEC = StreamCodec.unit(new FillBottlePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
