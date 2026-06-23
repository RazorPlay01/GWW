package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.Cuadro2Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Cuadro2EntityModel extends GeoModel<Cuadro2Entity> {
    @Override
    public ResourceLocation getModelResource(Cuadro2Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/cuadro_lilith_2.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Cuadro2Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/cuadro_lilith_2.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Cuadro2Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/cuadro_lilith_2.animation.json");
    }
}
