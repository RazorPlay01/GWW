package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.PanelEnergiaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PanelEnergiaEntityRenderer extends GeoEntityRenderer<PanelEnergiaEntity> {
    public PanelEnergiaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PanelEnergiaEntityModel());
    }
}
