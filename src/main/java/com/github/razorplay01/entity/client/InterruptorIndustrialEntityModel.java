package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.InterruptorIndustrialEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class InterruptorIndustrialEntityModel extends GeoModel<InterruptorIndustrialEntity> {
    @Override
    public ResourceLocation getModelResource(InterruptorIndustrialEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/interruptor_industrial.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(InterruptorIndustrialEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/interruptor_industrial.png");
    }

    @Override
    public ResourceLocation getAnimationResource(InterruptorIndustrialEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/interruptor_industrial.animation.json");
    }
}
