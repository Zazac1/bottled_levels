package com.zazac1.revampedxpbottle;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public class ModComponents {

    /**
     * Nombre de niveaux stockés dans une bouteille (0-30).
     * Les bouteilles avec le même STORED_LEVELS stackent automatiquement.
     */
    public static final DataComponentType<Integer> STORED_LEVELS = DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .build();

    public static void register() {
        Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                RevampedXpBottleMod.id("stored_levels"),
                STORED_LEVELS
        );
    }
}
