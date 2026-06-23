package com.github.razorplay01.entity.attribute;

import com.github.razorplay01.GWW;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class ModAttributes {
    public static final Holder<Attribute> CUADRO_VALUE;

    public static void register() {
        GWW.LOGGER.info("Registering Mod Attributes for " + GWW.MOD_ID);
    }

    private static Holder<Attribute> register(String string, Attribute attribute) {
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, string), attribute);
    }

    static {
        CUADRO_VALUE = register("gww.cuadro_value", (new RangedAttribute("attribute.name.gww.cuadro_value", 0, -1.0, 1.0)).setSyncable(true));
    }
}