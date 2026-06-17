package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.CannonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CannonEntityModel extends GeoModel<CannonEntity> {
    @Override
    public ResourceLocation getModelResource(CannonEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/cannon.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CannonEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/cannon.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CannonEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/cannon.animation.json");
    }
}
