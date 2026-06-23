package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.PuertaMetalicaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PuertaMetalicaEntityModel extends GeoModel<PuertaMetalicaEntity> {
    @Override
    public ResourceLocation getModelResource(PuertaMetalicaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/puerta_metalica.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PuertaMetalicaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/puerta_metalica.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PuertaMetalicaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/puerta_metalica.animation.json");
    }
}
