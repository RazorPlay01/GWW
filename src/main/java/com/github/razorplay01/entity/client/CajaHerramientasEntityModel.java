package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.CajaHerramientasEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CajaHerramientasEntityModel extends GeoModel<CajaHerramientasEntity> {
    @Override
    public ResourceLocation getModelResource(CajaHerramientasEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/caja_herramientas.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CajaHerramientasEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/caja_herramientas.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CajaHerramientasEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/caja_herramientas.animation.json");
    }
}
