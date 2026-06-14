package com.github.razorplay01.cam.api;

public interface ICameraPlugin {
    void initialize(ICameraModifier modifier);

    void update();
}
