package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.PanelEnergiaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PanelEnergiaEntityModel extends GeoModel<PanelEnergiaEntity> {
    @Override
    public ResourceLocation getModelResource(PanelEnergiaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/panel_energia.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PanelEnergiaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/panel_energia.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PanelEnergiaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/panel_energia.animation.json");
    }
}
