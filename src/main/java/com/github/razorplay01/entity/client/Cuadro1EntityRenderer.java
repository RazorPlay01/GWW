package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.Cuadro1Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Cuadro1EntityRenderer extends GeoEntityRenderer<Cuadro1Entity> {
    public Cuadro1EntityRenderer(EntityRendererProvider.Context context) {
        super(context, new Cuadro1EntityModel());
    }
}