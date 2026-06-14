package com.github.razorplay01.extra;

import net.minecraft.world.phys.Vec3;

public class ClientMinigameState {
    private static ClientMinigameState instance;

    private boolean active;
    private Vec3 center;
    private double radius;

    public static ClientMinigameState get() {
        if (instance == null) instance = new ClientMinigameState();
        return instance;
    }

    public void update(boolean active, Vec3 center, double radius) {
        this.active = active;
        this.center = center;
        this.radius = radius;
    }

    public boolean isActive() {
        return active;
    }

    public Vec3 getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }
}
