package com.github.razorplay01.cam;

public interface ICameraExtension {
    default void setFov(float fov) {
    }

    default float getFov() {
        return 0;
    }
}
