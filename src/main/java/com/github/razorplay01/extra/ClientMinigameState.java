package com.github.razorplay01.extra;

import lombok.Getter;
import net.minecraft.world.phys.Vec3;

@Getter
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
}
