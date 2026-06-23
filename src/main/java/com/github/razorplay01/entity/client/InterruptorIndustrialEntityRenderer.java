package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.InterruptorIndustrialEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class InterruptorIndustrialEntityRenderer extends GeoEntityRenderer<InterruptorIndustrialEntity> {
    public InterruptorIndustrialEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new InterruptorIndustrialEntityModel());
    }
}
