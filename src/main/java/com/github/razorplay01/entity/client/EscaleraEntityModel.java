package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.EscaleraEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EscaleraEntityModel extends GeoModel<EscaleraEntity> {
    @Override
    public ResourceLocation getModelResource(EscaleraEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/escalera.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EscaleraEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/escalera.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EscaleraEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/escalera.animation.json");
    }
}
