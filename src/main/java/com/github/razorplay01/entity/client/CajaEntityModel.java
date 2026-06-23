package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.CajaEntity;
import com.github.razorplay01.entity.custom.UblablaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CajaEntityModel extends GeoModel<CajaEntity> {
    @Override
    public ResourceLocation getModelResource(CajaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/caja.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CajaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/caja.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CajaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/caja.animation.json");
    }
}
