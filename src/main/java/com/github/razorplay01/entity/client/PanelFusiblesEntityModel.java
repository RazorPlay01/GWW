package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.PanelFusiblesEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PanelFusiblesEntityModel extends GeoModel<PanelFusiblesEntity> {
    @Override
    public ResourceLocation getModelResource(PanelFusiblesEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/panel_fusibles.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PanelFusiblesEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/panel_fusibles.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PanelFusiblesEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/panel_fusibles.animation.json");
    }
}
