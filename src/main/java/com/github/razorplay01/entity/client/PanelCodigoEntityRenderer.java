package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.PanelCodigoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.FastBoneFilterGeoLayer;

import java.util.Arrays;

public class PanelCodigoEntityRenderer extends GeoEntityRenderer<PanelCodigoEntity> {
    public PanelCodigoEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PanelCodigoEntityModel());
        this.addRenderLayer(new FastBoneFilterGeoLayer<>(this,
                () -> Arrays.asList(
                        "apagado",
                        "encendido_cerrado",
                        "encendido_abierto"
                ),
                (bone, entity, partialTick) -> {
                    // Ahora usamos los estados separados: powered y solved
                    boolean powered = entity.isPowered();
                    boolean solved = entity.isSolved();
                    String name = bone.getName();

                    boolean shouldShow = false;
                    if (!powered) {
                        shouldShow = name.equalsIgnoreCase("apagado");
                    } else if (solved) {
                        shouldShow = name.equalsIgnoreCase("encendido_abierto");
                    } else {
                        shouldShow = name.equalsIgnoreCase("encendido_cerrado");
                    }
                    bone.setHidden(!shouldShow);
                }
        ));
    }
}