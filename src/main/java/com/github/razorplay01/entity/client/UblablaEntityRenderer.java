package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.CannonEntity;
import com.github.razorplay01.entity.custom.UblablaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class UblablaEntityRenderer extends GeoEntityRenderer<UblablaEntity> {
    public UblablaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new UblablaEntityModel());
    }
}
