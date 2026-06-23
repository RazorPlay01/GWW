package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.CannonEntity;
import com.github.razorplay01.entity.custom.UblablaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class UblablaEntityModel extends GeoModel<UblablaEntity> {
    @Override
    public ResourceLocation getModelResource(UblablaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/ublabla.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(UblablaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/ublabla.png");
    }

    @Override
    public ResourceLocation getAnimationResource(UblablaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/ublabla.animation.json");
    }
}
