package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.CannonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CannonEntityRenderer extends GeoEntityRenderer<CannonEntity> {
    public CannonEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CannonEntityModel());
    }
}
