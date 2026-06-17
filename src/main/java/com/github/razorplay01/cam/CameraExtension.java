package com.github.razorplay01.cam;

public interface CameraExtension {
    default void setFov(int fov) {
    }

    int getFov();

    void setCustomRotation(float yaw, float pitch, float roll);
    void setCustomPosition(double x, double y, double z);

    float getRoll();
}
