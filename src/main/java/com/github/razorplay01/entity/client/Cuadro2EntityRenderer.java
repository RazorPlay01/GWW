package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.Cuadro2Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Cuadro2EntityRenderer extends GeoEntityRenderer<Cuadro2Entity> {
    public Cuadro2EntityRenderer(EntityRendererProvider.Context context) {
        super(context, new Cuadro2EntityModel());
    }
}