package com.github.razorplay01.cam;

import com.github.razorplay01.cam.api.CameraPlugin;
import com.github.razorplay01.cam.api.ICameraModifier;
import com.github.razorplay01.cam.api.ICameraPlugin;

//@CameraPlugin(value = "dev")
public class DevPlugin implements ICameraPlugin {
    private ICameraModifier modifier;

    @Override
    public void initialize(ICameraModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public void update() {
        modifier.enable()
                .enablePos()
                .enableRotation()
                .setToVanilla()
                .addPos(0, 1, 0);
    }
}