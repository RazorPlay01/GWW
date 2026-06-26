package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.PanelCodigoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PanelCodigoEntityRenderer extends GeoEntityRenderer<PanelCodigoEntity> {
    public PanelCodigoEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PanelCodigoEntityModel());
    }
}
