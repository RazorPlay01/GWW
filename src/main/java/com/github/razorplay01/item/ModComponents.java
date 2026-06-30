package com.github.razorplay01.item;

import com.github.razorplay01.GWW;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Codec;

public class ModComponents {
    private ModComponents() {
        /* This utility class should not be instantiated */
    }

    public static final DataComponentType<String> PISTA_COMMAND = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "pista_command"),
            DataComponentType.<String>builder()
                    .persistent(Codec.STRING)           // para que se guarde en NBT / /give
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8) // sincronización en red
                    .build()
    );

    public static void register() {
        GWW.LOGGER.info("Registering Mod Components for " + GWW.MOD_ID);
    }
}