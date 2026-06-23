package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.FigurasParedEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FigurasParedEntityRenderer extends GeoEntityRenderer<FigurasParedEntity> {
    public FigurasParedEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new FigurasParedEntityModel());
    }
}
