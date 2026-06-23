package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.RejaDuctoEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RejaDuctoEntityModel extends GeoModel<RejaDuctoEntity> {
    @Override
    public ResourceLocation getModelResource(RejaDuctoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/reja_ducto.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RejaDuctoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/reja_ducto.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RejaDuctoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/reja_ducto.animation.json");
    }
}
