package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.LuzTortugaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LuzTortugaEntityModel extends GeoModel<LuzTortugaEntity> {

    @Override
    public ResourceLocation getModelResource(LuzTortugaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/luz_tortuga.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LuzTortugaEntity animatable) {
        return switch (animatable.getState()) {
            case 1 -> ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/luz_tortuga_rojo.png");
            case 2 -> ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/luz_tortuga_verde.png");
            default -> ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/luz_tortuga_apagada.png");
        };
    }

    @Override
    public ResourceLocation getAnimationResource(LuzTortugaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/luz_tortuga.animation.json");
    }
}