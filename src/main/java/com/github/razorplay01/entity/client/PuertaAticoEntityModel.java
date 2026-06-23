package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.PuertaAticoEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PuertaAticoEntityModel extends GeoModel<PuertaAticoEntity> {
    @Override
    public ResourceLocation getModelResource(PuertaAticoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/puerta_atico.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PuertaAticoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/puerta_atico.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PuertaAticoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/puerta_atico.animation.json");
    }
}
