package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.CajaEntity;
import com.github.razorplay01.entity.custom.UblablaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CajaEntityRenderer extends GeoEntityRenderer<CajaEntity> {
    public CajaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CajaEntityModel());
    }
}
