package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.PuertaMetalicaUblablaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PuertaMetalicaUblablaEntityModel extends GeoModel<PuertaMetalicaUblablaEntity> {
    @Override
    public ResourceLocation getModelResource(PuertaMetalicaUblablaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/puerta_metalica_ublabla.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PuertaMetalicaUblablaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/puerta_metalica_ublabla.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PuertaMetalicaUblablaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/puerta_metalica_ublabla.animation.json");
    }
}
