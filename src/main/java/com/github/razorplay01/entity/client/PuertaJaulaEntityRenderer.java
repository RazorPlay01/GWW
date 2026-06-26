package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.PuertaJaulaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PuertaJaulaEntityRenderer extends GeoEntityRenderer<PuertaJaulaEntity> {
    public PuertaJaulaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PuertaJaulaEntityModel());
    }
}
