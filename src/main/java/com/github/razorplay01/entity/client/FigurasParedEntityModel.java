package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.FigurasParedEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FigurasParedEntityModel extends GeoModel<FigurasParedEntity> {

    @Override
    public ResourceLocation getModelResource(FigurasParedEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/figuras_pared.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FigurasParedEntity animatable) {
        return switch (animatable.getState()) {
            case 1 -> ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/triangulo.png");
            case 2 -> ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/hexagono.png");
            case 3 -> ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/pentagono.png");
            default -> ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/cuadrado.png");
        };
    }

    @Override
    public ResourceLocation getAnimationResource(FigurasParedEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/figuras_pared.animation.json");
    }
}