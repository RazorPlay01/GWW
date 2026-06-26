package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.EscaleraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EscaleraEntityRenderer extends GeoEntityRenderer<EscaleraEntity> {
    public EscaleraEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new EscaleraEntityModel());
    }
}
