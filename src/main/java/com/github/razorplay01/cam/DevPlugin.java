package com.github.razorplay01.cam;

import com.github.razorplay01.cam.api.CameraModifier;
import com.github.razorplay01.cam.api.CameraPlugin;
import com.github.razorplay01.cam.api.Plugin;

//@Plugin(value = "dev")
public class DevPlugin implements CameraPlugin {
    private final CameraModifier modifier;

    public DevPlugin(CameraModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public void update(float partialTicks) {
        modifier.enable()
                .enablePos()
                .enableRotation()
                .setToVanilla()
                .addPos(0, 1, 0);
    }
}