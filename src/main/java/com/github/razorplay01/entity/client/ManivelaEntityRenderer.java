package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.ManivelaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ManivelaEntityRenderer extends GeoEntityRenderer<ManivelaEntity> {
    public ManivelaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new ManivelaEntityModel());
    }
}
