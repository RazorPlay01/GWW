package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.PuertaMetalicaUblablaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PuertaMetalicaUblablaEntityRenderer extends GeoEntityRenderer<PuertaMetalicaUblablaEntity> {
    public PuertaMetalicaUblablaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PuertaMetalicaUblablaEntityModel());
    }
}
