package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.PuertaJaulaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PuertaJaulaEntityModel extends GeoModel<PuertaJaulaEntity> {
    @Override
    public ResourceLocation getModelResource(PuertaJaulaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/puerta_jaula.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PuertaJaulaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/puerta_jaula.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PuertaJaulaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/puerta_jaula.animation.json");
    }
}
