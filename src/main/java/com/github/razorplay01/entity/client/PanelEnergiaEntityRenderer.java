package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.PanelEnergiaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.FastBoneFilterGeoLayer;

import java.util.List;

public class PanelEnergiaEntityRenderer extends GeoEntityRenderer<PanelEnergiaEntity> {
    public PanelEnergiaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PanelEnergiaEntityModel());
        this.addRenderLayer(new FastBoneFilterGeoLayer<>(this,
                () -> List.of("cable_completo", "cable_cortado"),
                (bone, entity, partialTick) -> {
                    if (entity.isActive()) {
                        switch (bone.getName()) {
                            case "cable_completo" -> bone.setHidden(true);
                            case "cable_cortado" -> bone.setHidden(false);
                            default -> {
                            }
                        }
                    } else {
                        switch (bone.getName()) {
                            case "cable_completo" -> bone.setHidden(false);
                            case "cable_cortado" -> bone.setHidden(true);
                            default -> {
                            }
                        }
                    }
                }
        ));
    }
}
