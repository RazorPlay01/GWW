package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.PalancaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PalancaEntityModel extends GeoModel<PalancaEntity> {
    @Override
    public ResourceLocation getModelResource(PalancaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/palanca.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PalancaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/palanca.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PalancaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/palanca.animation.json");
    }
}
