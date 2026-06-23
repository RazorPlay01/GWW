package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.ManivelaEntity;
import com.github.razorplay01.entity.custom.PalancaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PalancaEntityRenderer extends GeoEntityRenderer<PalancaEntity> {
    public PalancaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PalancaEntityModel());
    }
}
