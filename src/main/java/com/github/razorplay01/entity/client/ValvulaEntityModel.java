package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.ValvulaEntity;
import com.github.razorplay01.entity.custom.util.ValvulaType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ValvulaEntityModel extends GeoModel<ValvulaEntity> {
    @Override
    public ResourceLocation getModelResource(ValvulaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/valvula.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ValvulaEntity animatable) {
        ValvulaType type = animatable.getValType();
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, type.getTexturePath());
    }

    @Override
    public ResourceLocation getAnimationResource(ValvulaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/valvula.animation.json");
    }
}