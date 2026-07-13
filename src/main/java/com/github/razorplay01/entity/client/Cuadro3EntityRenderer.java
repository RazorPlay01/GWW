package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.Cuadro3Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Cuadro3EntityRenderer extends GeoEntityRenderer<Cuadro3Entity> {
    public Cuadro3EntityRenderer(EntityRendererProvider.Context context) {
        super(context, new Cuadro3EntityModel());
    }
}