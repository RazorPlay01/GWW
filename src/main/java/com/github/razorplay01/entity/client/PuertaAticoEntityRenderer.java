package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.PuertaAticoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PuertaAticoEntityRenderer extends GeoEntityRenderer<PuertaAticoEntity> {
    public PuertaAticoEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PuertaAticoEntityModel());
    }
}
