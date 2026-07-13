package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.Cuadro3Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Cuadro3EntityModel extends GeoModel<Cuadro3Entity> {
    @Override
    public ResourceLocation getModelResource(Cuadro3Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/cuadro_lilith_3.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Cuadro3Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/cuadro_lilith_3.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Cuadro3Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/cuadro_lilith_3.animation.json");
    }
}
