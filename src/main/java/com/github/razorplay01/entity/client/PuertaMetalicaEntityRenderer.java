package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.PuertaMetalicaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PuertaMetalicaEntityRenderer extends GeoEntityRenderer<PuertaMetalicaEntity> {
    public PuertaMetalicaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PuertaMetalicaEntityModel());
    }
}
