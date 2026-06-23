package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.LuzTortugaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LuzTortugaEntityRenderer extends GeoEntityRenderer<LuzTortugaEntity> {
    public LuzTortugaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new LuzTortugaEntityModel());
    }
}
