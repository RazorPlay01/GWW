package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.PanelCodigoEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PanelCodigoEntityModel extends GeoModel<PanelCodigoEntity> {
    @Override
    public ResourceLocation getModelResource(PanelCodigoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/panel_codigo.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PanelCodigoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/panel_codigo.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PanelCodigoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/panel_codigo.animation.json");
    }
}
