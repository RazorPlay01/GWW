package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.CajaEntity;
import com.github.razorplay01.entity.custom.Cuadro1Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Cuadro1EntityModel extends GeoModel<Cuadro1Entity> {
    @Override
    public ResourceLocation getModelResource(Cuadro1Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/cuadro_lilith_1.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Cuadro1Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/cuadro_lilith_1.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Cuadro1Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/cuadro_lilith_1.animation.json");
    }
}
