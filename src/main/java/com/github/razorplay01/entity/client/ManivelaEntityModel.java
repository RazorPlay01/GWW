package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.ManivelaEntity;
import com.github.razorplay01.entity.custom.util.ValvulaType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ManivelaEntityModel extends GeoModel<ManivelaEntity> {
    @Override
    public ResourceLocation getModelResource(ManivelaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/manivela.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ManivelaEntity animatable) {
        ValvulaType type = animatable.getValType();
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, type.getTexturePath());
    }

    @Override
    public ResourceLocation getAnimationResource(ManivelaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/manivela.animation.json");
    }
}
